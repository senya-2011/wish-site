import {
  Box,
  Button,
  FormControl,
  FormLabel,
  HStack,
  Image,
  Input,
  Text,
  VStack,
} from "@chakra-ui/react";
import { useState, useRef } from "react";

type PhotoUploadProps = {
  currentPhotoUrl?: string;
  onFileChange: (file: File | null) => void;
  onPhotoUrlChange?: (url: string) => void;
};

export const PhotoUpload = ({ 
  currentPhotoUrl, 
  onFileChange,
  onPhotoUrlChange 
}: PhotoUploadProps) => {
  const [previewUrl, setPreviewUrl] = useState<string | null>(currentPhotoUrl || null);
  const [uploadError, setUploadError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileSelect = (event: React.ChangeEvent<HTMLInputElement>) => {
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
    
    const reader = new FileReader();
    reader.onloadend = () => {
      setPreviewUrl(reader.result as string);
    };
    reader.readAsDataURL(file);
    
    onFileChange(file);
  };

  const handleRemovePhoto = () => {
    setPreviewUrl(null);
    onFileChange(null);
    if (onPhotoUrlChange) {
      onPhotoUrlChange("");
    }
    if (fileInputRef.current) {
      fileInputRef.current.value = "";
    }
  };

  return (
    <FormControl>
      <FormLabel>Фото</FormLabel>
      <VStack align="stretch" spacing={3}>
        {(previewUrl || currentPhotoUrl) && (
          <Box position="relative" borderRadius="md" overflow="hidden" maxW="300px">
            <Image
              src={previewUrl || currentPhotoUrl}
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
            display="none"
            id="photo-upload"
          />
          <Button
            as="label"
            htmlFor="photo-upload"
            cursor="pointer"
            colorScheme="blue"
            variant="outline"
            size="sm"
          >
            Выбрать файл
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

