import {
  Alert,
  AlertIcon,
  Badge,
  Box,
  Button,
  ButtonGroup,
  Card,
  CardBody,
  CardHeader,
  Flex,
  FormControl,
  FormLabel,
  Heading,
  Input,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  Skeleton,
  Stack,
  Table,
  Tbody,
  Td,
  Text,
  Textarea,
  Th,
  Thead,
  Tr,
  useDisclosure,
} from "@chakra-ui/react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { ChangeEvent } from "react";
import type { WishRequest, WishResponse, WishPageResponse } from "../api";
import { wishesApi } from "../lib/api-client";
import { useAuth } from "../context/useAuth";

const PAGE_SIZE = 10;

export const MyWishesPage = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const [page, setPage] = useState(1);
  const [form, setForm] = useState({
    title: "",
    description: "",
    photoUrl: "",
    price: "",
  });
  const { isOpen, onOpen, onClose } = useDisclosure();

  const userId = user?.user_id;

  const wishesQuery = useQuery<WishPageResponse>({
    queryKey: ["my-wishes", userId, page],
    queryFn: async () => {
      if (!userId) {
        throw new Error("Пользователь не найден");
      }
      const response = await wishesApi.getUserWishes(userId, page - 1, PAGE_SIZE);
      return response.data;
    },
    enabled: Boolean(userId),
    placeholderData: keepPreviousData,
  });

  const totalPages = Math.max(1, wishesQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);

  const items: WishResponse[] = wishesQuery.data?.items ?? [];

  const createMutation = useMutation({
    mutationFn: async () => {
      if (!userId) {
        throw new Error("Пользователь не найден");
      }
      const payload: WishRequest = {
        title: form.title,
        description: form.description || undefined,
        photo_url: form.photoUrl || undefined,
        price: form.price ? Number(form.price) : undefined,
      };
      const response = await wishesApi.createWish(userId, payload);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      setForm({
        title: "",
        description: "",
        photoUrl: "",
        price: "",
      });
      setPage(1);
      onClose();
    },
  });

  const handleSubmit = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    createMutation.mutate();
  };

  const isEmpty = !wishesQuery.isLoading && items.length === 0;

  return (
    <>
      <Card borderRadius="2xl" shadow="xl">
        <CardHeader>
          <Flex justify="space-between" align={{ base: "stretch", md: "center" }} gap={4} direction={{ base: "column", md: "row" }}>
            <Box>
              <Heading size="md">Мои желания</Heading>
              <Text color="gray.500">Все желания, созданные вами. По 10 штук на страницу.</Text>
            </Box>
            <Button colorScheme="purple" onClick={onOpen}>
              Создать желание
            </Button>
          </Flex>
        </CardHeader>
        <CardBody>
          {wishesQuery.isLoading ? (
            <Stack spacing={4}>
              {Array.from({ length: 4 }).map((_, index) => (
                <Skeleton key={index} height="64px" borderRadius="md" />
              ))}
            </Stack>
          ) : wishesQuery.isError ? (
            <Alert status="error">
              <AlertIcon />
              Не удалось загрузить желания. Попробуйте позже.
            </Alert>
          ) : isEmpty ? (
            <Box textAlign="center" py={10}>
              <Heading size="md" mb={2}>
                Здесь пока пусто
              </Heading>
              <Text color="gray.500">Добавьте первое желание с помощью кнопки выше.</Text>
            </Box>
          ) : (
            <>
              <Table variant="simple">
                <Thead>
                  <Tr>
                    <Th>ID</Th>
                    <Th>Название</Th>
                    <Th>Цена</Th>
                    <Th>Создано</Th>
                  </Tr>
                </Thead>
                <Tbody>
                  {items.map((wish: WishResponse) => (
                    <Tr key={wish.wish_id}>
                      <Td>
                        <Badge colorScheme="purple" fontSize="sm">
                          #{wish.wish_id}
                        </Badge>
                      </Td>
                      <Td>
                        <Text fontWeight="semibold">{wish.title}</Text>
                        {wish.description && (
                          <Text color="gray.500" noOfLines={2}>
                            {wish.description}
                          </Text>
                        )}
                      </Td>
                      <Td>{wish.price ? `${wish.price} ₽` : "—"}</Td>
                      <Td>{new Date(wish.created_at).toLocaleString()}</Td>
                    </Tr>
                  ))}
                </Tbody>
              </Table>

              <Flex justify="space-between" align="center" mt={6} direction={{ base: "column", md: "row" }} gap={4}>
                <Text color="gray.600">
                  Страница {safePage} из {totalPages}
                </Text>
                <ButtonGroup>
                  <Button onClick={() => setPage((prev) => Math.max(1, prev - 1))} isDisabled={page === 1}>
                    Назад
                  </Button>
                  <Button onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))} isDisabled={page >= totalPages}>
                    Вперёд
                  </Button>
                </ButtonGroup>
              </Flex>
            </>
          )}
        </CardBody>
      </Card>

      <Modal isOpen={isOpen} onClose={onClose} size="lg">
        <ModalOverlay />
        <ModalContent as="form" onSubmit={handleSubmit}>
          <ModalHeader>Новое желание</ModalHeader>
          <ModalCloseButton />
          <ModalBody>
            <Stack spacing={4}>
              <FormControl isRequired>
                <FormLabel>Название</FormLabel>
                <Input
                  value={form.title}
                  onChange={(event: ChangeEvent<HTMLInputElement>) =>
                    setForm((prev) => ({ ...prev, title: event.target.value }))
                  }
                />
              </FormControl>
              <FormControl>
                <FormLabel>Описание</FormLabel>
                <Textarea
                  value={form.description}
                  onChange={(event: ChangeEvent<HTMLTextAreaElement>) =>
                    setForm((prev) => ({ ...prev, description: event.target.value }))
                  }
                  rows={4}
                />
              </FormControl>
              <FormControl>
                <FormLabel>Ссылка на фото</FormLabel>
                <Input
                  value={form.photoUrl}
                  onChange={(event: ChangeEvent<HTMLInputElement>) =>
                    setForm((prev) => ({ ...prev, photoUrl: event.target.value }))
                  }
                />
              </FormControl>
              <FormControl>
                <FormLabel>Цена (₽)</FormLabel>
                <Input
                  type="number"
                  step="0.01"
                  value={form.price}
                  onChange={(event: ChangeEvent<HTMLInputElement>) =>
                    setForm((prev) => ({ ...prev, price: event.target.value }))
                  }
                />
              </FormControl>
              {createMutation.isError && (
                <Alert status="error">
                  <AlertIcon />
                  Не удалось сохранить желание.
                </Alert>
              )}
            </Stack>
          </ModalBody>
          <ModalFooter gap={3}>
            <Button variant="ghost" onClick={onClose}>
              Отмена
            </Button>
            <Button colorScheme="purple" type="submit" isLoading={createMutation.isPending} isDisabled={!form.title}>
              Создать
            </Button>
          </ModalFooter>
        </ModalContent>
      </Modal>
    </>
  );
};

