import {
  Box,
  Button,
  Divider,
  Flex,
  Heading,
  Image,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  Stack,
  Text,
} from "@chakra-ui/react";
import type { WishResponse } from "../../api";

type WishDetailsModalProps = {
  wish: WishResponse | null;
  isOpen: boolean;
  onClose: () => void;
  onEdit: (wish: WishResponse) => void;
};

export const WishDetailsModal = ({ wish, isOpen, onClose, onEdit }: WishDetailsModalProps) => (
  <Modal isOpen={isOpen} onClose={onClose} size="3xl">
    <ModalOverlay />
    <ModalContent>
      <ModalHeader>Детали желания</ModalHeader>
      <ModalCloseButton />
      <ModalBody>
        {wish && (
          <Flex direction={{ base: "column", md: "row" }} gap={6}>
            <Box flex="0 0 280px">
              {wish.photo_url ? (
                <Image
                  src={wish.photo_url}
                  alt={wish.title}
                  borderRadius="lg"
                  objectFit="cover"
                  w="100%"
                  maxH="260px"
                />
              ) : (
                <Box
                  border="1px dashed"
                  borderColor="gray.300"
                  borderRadius="lg"
                  h="260px"
                  display="flex"
                  alignItems="center"
                  justifyContent="center"
                  color="gray.400"
                >
                  Нет изображения
                </Box>
              )}
            </Box>
            <Box flex="1">
              <Heading size="md" mb={2}>
                {wish.title}
              </Heading>
              <Text color="gray.600" mb={4}>
                Создано: {new Date(wish.created_at).toLocaleDateString("ru-RU", {
                  day: "numeric",
                  month: "long",
                  year: "numeric"
                })}{" "}
                в {new Date(wish.created_at).toLocaleTimeString("ru-RU", {
                  hour: "2-digit",
                  minute: "2-digit"
                })}
              </Text>
              <Stack spacing={3}>
                <Box>
                  <Text fontSize="sm" color="gray.500">
                    Цена
                  </Text>
                  <Text fontWeight="semibold">{wish.price ? `${wish.price} ₽` : "—"}</Text>
                </Box>
                <Divider />
                <Box>
                  <Text fontSize="sm" color="gray.500" mb={1}>
                    Описание
                  </Text>
                  <Text color="gray.700">{wish.description || "Описание отсутствует"}</Text>
                </Box>
              </Stack>
            </Box>
          </Flex>
        )}
      </ModalBody>
      <ModalFooter>
        <Button variant="ghost" mr={3} onClick={onClose}>
          Закрыть
        </Button>
        {wish && (
          <Button
            colorScheme="purple"
            onClick={() => {
              onEdit(wish);
              onClose();
            }}
          >
            Изменить
          </Button>
        )}
      </ModalFooter>
    </ModalContent>
  </Modal>
);

