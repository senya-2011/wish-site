# Тест загрузки файла в MinIO через API
# Создай тестовое изображение или используй существующее

$token = "YOUR_JWT_TOKEN_HERE"  # Получи токен через /api/auth/login
$filePath = "test-image.jpg"    # Путь к тестовому изображению

if (-not (Test-Path $filePath)) {
    Write-Host "Создай тестовое изображение или укажи путь к существующему файлу"
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
}

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/files/upload" `
        -Method Post `
        -Headers $headers `
        -InFile $filePath `
        -ContentType "multipart/form-data"
    
    Write-Host "✅ Файл успешно загружен!"
    Write-Host "File URL: $($response.file_url)"
    Write-Host "Object Name: $($response.object_name)"
    Write-Host ""
    Write-Host "Проверь доступность файла:"
    Write-Host "curl $($response.file_url)"
} catch {
    Write-Host "❌ Ошибка загрузки: $($_.Exception.Message)"
    Write-Host $_.Exception.Response
}

