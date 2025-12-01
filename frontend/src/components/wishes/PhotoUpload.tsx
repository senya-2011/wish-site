import {
  Box,
  Button,
  FormControl,
  FormLabel,
  HStack,
  Image,
  Input,
  Spinner,
  Text,
  VStack,
} from "@chakra-ui/react";
import { useState, useRef } from "react";
import { filesApi } from "../../lib/api-client";

type PhotoUploadProps = {
  currentPhotoUrl?: string;
  onPhotoUrlChange: (url: string) => void;
};

export const PhotoUpload = ({ currentPhotoUrl, onPhotoUrlChange }: PhotoUploadProps) => {
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = async (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setUploadError("Можно загружать только изображения");
      return;
    }

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setUploadError("Размер файла не должен превышать 10MB");
      return;
    }

    setUploadError(null);
    setIsUploading(true);

    try {
      const formData = new FormData();
      formData.append("file", file);

      const response = await filesApi.uploadFile(file);
      const fileUrl = response.data.file_url;

      if (fileUrl) {
        onPhotoUrlChange(fileUrl);
      }
    } catch (error) {
      console.error("Upload error:", error);
      setUploadError("Не удалось загрузить файл");
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    }
  };

  const handleRemovePhoto = () => {
    onPhotoUrlChange("");
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <FormControl>
      <FormLabel>Фото</FormLabel>
      <VStack align="stretch" spacing={3}>
        {currentPhotoUrl && (
          <Box position="relative" borderRadius="md" overflow="hidden" maxW="300px">
            <Image
              src={currentPhotoUrl}
              alt="Preview"
              objectFit="cover"
              maxH="200px"
              w="100%"
            />
            <Button
              size="sm"
              colorScheme="red"
              position="absolute"
              top={2}
              right={2}
              onClick={handleRemovePhoto}
            >
              Удалить
            </Button>
          </Box>
        )}

        <HStack>
          <Input
            ref={fileInputRef}
            type="file"
            accept="image/*"
            onChange={handleFileSelect}
            disabled={isUploading}
            display="none"
            id="photo-upload"
          />
          <Button
            as="label"
            htmlFor="photo-upload"
            cursor="pointer"
            isDisabled={isUploading}
            colorScheme="blue"
            variant="outline"
            size="sm"
          >
            {isUploading ? (
              <>
                <Spinner size="sm" mr={2} />
                Загрузка...
              </>
            ) : (
              "Выбрать файл"
            )}
          </Button>
          <Text fontSize="sm" color="gray.500">
            Максимум 10MB, только изображения
          </Text>
        </HStack>

        {uploadError && (
          <Text fontSize="sm" color="red.500">
            {uploadError}
          </Text>
        )}
      </VStack>
    </FormControl>
  );
};

