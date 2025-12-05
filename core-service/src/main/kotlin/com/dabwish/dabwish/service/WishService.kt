package com.dabwish.dabwish.service

import com.dabwish.dabwish.events.WishEventPublisher
import com.dabwish.dabwish.exception.UserNotFoundException
import com.dabwish.dabwish.exception.WishNotFoundException
import com.dabwish.dabwish.generated.dto.WishRequest
import com.dabwish.dabwish.generated.dto.WishUpdateRequest
import com.dabwish.dabwish.mapper.WishMapper
import com.dabwish.dabwish.model.wish.Wish
import com.dabwish.dabwish.repository.UserRepository
import com.dabwish.dabwish.repository.WishRepository
import com.dabwish.dabwish.util.TransactionUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
class WishService(
    private val wishRepository: WishRepository,
    private val wishMapper: WishMapper,
    private val userRepository: UserRepository,
    @Autowired(required = false) private val wishEventPublisher: WishEventPublisher?,
    private val minioService: MinioService,
) {

    private fun isLastReferenceToPhoto(url: String): Boolean {
        return wishRepository.countByPhotoUrl(url) <= 1
    }

    private fun deleteFileQuietly(objectNameOrUrl: String?) {
        if (objectNameOrUrl.isNullOrBlank()) return
        try {
            val objectName = if (objectNameOrUrl.startsWith("http")) {
                minioService.extractObjectNameFromUrl(objectNameOrUrl)
            } else {
                objectNameOrUrl
            }
            objectName?.let { minioService.deleteFile(it) }
        } catch (e: Exception) {
        }
    }

    private fun uploadFileAndRegisterRollback(file: MultipartFile?): String? {
        if (file == null || file.isEmpty) return null

        val objectName = minioService.uploadFile(file, prefix = "wishes/")

        TransactionUtil.afterRollback {
            deleteFileQuietly(objectName)
        }

        return minioService.getFileUrl(objectName)
    }

    private fun scheduleOldFileCleanup(url: String?) {
        if (url != null && isLastReferenceToPhoto(url)) {
            TransactionUtil.afterCommit {
                deleteFileQuietly(url)
            }
        }
    }

    @Cacheable(
        cacheNames = ["userWishes"],
        key = "#userId + ':' + #pageable.pageNumber + ':' + #pageable.pageSize + ':' + #pageable.sort.toString()",
    )
    fun findAllByUserId(userId: Long, pageable: Pageable): Page<Wish> {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException(userId)
        }
        return wishRepository.findAllByUserId(userId, pageable)
    }

    @Cacheable(cacheNames = ["wishesById"], key = "#id")
    fun findById(id: Long): Wish {
        return wishRepository.findById(id)
            .orElseThrow { WishNotFoundException(id) }
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun create(userId: Long, request: WishRequest): Wish {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }
        val wish = wishMapper.toEntity(request, user)
        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishCreated(saved)

        return saved
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun createWithFile(userId: Long, request: WishRequest, file: MultipartFile?): Wish {
        val user = userRepository.findById(userId)
            .orElseThrow { UserNotFoundException(userId) }

        val uploadedUrl = uploadFileAndRegisterRollback(file)

        val wish = wishMapper.toEntity(request, user)
        if (uploadedUrl != null) {
            wish.photoUrl = uploadedUrl
        } else {
            wish.photoUrl = request.photoUrl
        }

        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishCreated(saved)

        return saved
    }

    @Transactional
    @Caching(
        evict = [
            CacheEvict(cacheNames = ["wishesById"], key = "#id"),
            CacheEvict(cacheNames = ["userWishes"], allEntries = true),
        ],
    )
    fun delete(id: Long) {
        val wish = wishRepository.findById(id).orElseThrow { WishNotFoundException(id) }

        wishRepository.deleteById(id)
        scheduleOldFileCleanup(wish.photoUrl)
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun update(id: Long, request: WishUpdateRequest): Wish {
        val wish = wishRepository.findById(id).orElseThrow { WishNotFoundException(id) }

        val oldUrl = wish.photoUrl
        wishMapper.updateEntityFromRequest(request, wish)
        val newUrl = wish.photoUrl

        if (newUrl != oldUrl) {
            scheduleOldFileCleanup(oldUrl)
        }

        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishUpdated(saved)
        return saved
    }

    @Transactional
    @Caching(
        put = [CachePut(cacheNames = ["wishesById"], key = "#result.id")],
        evict = [CacheEvict(cacheNames = ["userWishes"], allEntries = true)],
    )
    fun updateWithFile(id: Long, request: WishUpdateRequest, file: MultipartFile?): Wish {
        val wish = wishRepository.findById(id).orElseThrow { WishNotFoundException(id) }

        val newPhotoUrl = uploadFileAndRegisterRollback(file)
        val oldPhotoUrl = wish.photoUrl

        wishMapper.updateEntityFromRequest(request, wish)

        if (newPhotoUrl != null) {
            wish.photoUrl = newPhotoUrl
            if (oldPhotoUrl != null && oldPhotoUrl != newPhotoUrl) {
                scheduleOldFileCleanup(oldPhotoUrl)
            }
        }

        val saved = wishRepository.save(wish)
        wishEventPublisher?.publishWishUpdated(saved)
        return saved
    }
}