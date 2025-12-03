import {
  Box,
  Button,
  Card,
  CardBody,
  Divider,
  Flex,
  Heading,
  Image,
  Spinner,
  Stack,
  Text,
  useDisclosure,
  useToast,
} from "@chakra-ui/react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { WishRequest, WishUpdateRequest } from "../../api";
import { wishesApi } from "../../lib/api-client";
import { useAuth } from "../../context/useAuth";
import type { WishFormState } from "./types";
import { EditWishModal } from "./EditWishModal";
import { DeleteWishDialog } from "./DeleteWishDialog";

const getDefaultFormState = (): WishFormState => ({
  title: "",
  description: "",
  photoUrl: "",
  price: "",
  photoFile: null,
});

export const WishDetailPage = () => {
  const { wishId } = useParams<{ wishId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const queryClient = useQueryClient();
  const toast = useToast();
  
  const [editForm, setEditForm] = useState<WishFormState>(getDefaultFormState);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [wishToDelete, setWishToDelete] = useState<number | null>(null);
  
  const editModal = useDisclosure();
  const deleteDialog = useDisclosure();

  const wishQuery = useQuery({
    queryKey: ["wish", wishId],
    queryFn: async () => {
      if (!wishId) throw new Error("Wish ID is required");
      const response = await wishesApi.getWishById(Number(wishId));
      return response.data;
    },
    enabled: Boolean(wishId),
  });

  const copyMutation = useMutation({
    mutationFn: async () => {
      if (!user?.user_id || !wishQuery.data) throw new Error("User or wish not found");
      
      const wish = wishQuery.data;
      const request: WishRequest = {
        title: wish.title,
        description: wish.description || undefined,
        photo_url: wish.photo_url || undefined,
        price: wish.price || undefined,
      };

      const response = await wishesApi.createWish(user.user_id, request);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      toast({
        title: "Желание добавлено",
        description: "Желание успешно добавлено в ваш список",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    },
    onError: (error: unknown) => {
      const errorMessage = error && typeof error === "object" && "response" in error
        ? (error as { response?: { data?: { message?: string } } })?.response?.data?.message
        : undefined;
      toast({
        title: "Ошибка",
        description: errorMessage || "Не удалось добавить желание",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    },
  });

  const updateMutation = useMutation({
    mutationFn: async () => {
      if (!editingId || !wishQuery.data) throw new Error("Wish not found");
      
      if (editForm.photoFile) {
        const response = await wishesApi.updateWishByIdWithFile(
          editingId,
          editForm.title || undefined,
          editForm.description || undefined,
          editForm.photoFile,
          editForm.price ? Number(editForm.price) : undefined
        );
        return response.data;
      } else {
        const payload: WishUpdateRequest = {
          title: editForm.title || undefined,
          description: editForm.description || undefined,
          photo_url: editForm.photoUrl || undefined,
          price: editForm.price ? Number(editForm.price) : undefined,
        };
        const response = await wishesApi.updateWishById(editingId, payload);
        return response.data;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["wish", wishId] });
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      editModal.onClose();
      toast({
        title: "Желание обновлено",
        description: "Изменения успешно сохранены",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    },
    onError: (error: unknown) => {
      const errorMessage = error && typeof error === "object" && "response" in error
        ? (error as { response?: { data?: { message?: string } } })?.response?.data?.message
        : undefined;
      toast({
        title: "Ошибка",
        description: errorMessage || "Не удалось обновить желание",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async () => {
      if (!wishToDelete) throw new Error("Wish ID is required");
      await wishesApi.deleteWishById(wishToDelete);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      deleteDialog.onClose();
      toast({
        title: "Желание удалено",
        description: "Желание успешно удалено",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
      navigate("/my-wishes");
    },
    onError: (error: unknown) => {
      const errorMessage = error && typeof error === "object" && "response" in error
        ? (error as { response?: { data?: { message?: string } } })?.response?.data?.message
        : undefined;
      toast({
        title: "Ошибка",
        description: errorMessage || "Не удалось удалить желание",
        status: "error",
        duration: 5000,
        isClosable: true,
      });
    },
  });

  const handleFormChange = (field: keyof WishFormState, value: string | File | null) => {
    setEditForm((prev) => ({ ...prev, [field]: value }));
  };

  const openEditModal = () => {
    if (!wishQuery.data) return;
    const wish = wishQuery.data;
    setEditingId(wish.wish_id);
    setEditForm({
      title: wish.title,
      description: wish.description ?? "",
      photoUrl: wish.photo_url ?? "",
      price: wish.price !== undefined ? String(wish.price) : "",
      photoFile: null,
    });
    editModal.onOpen();
  };

  const openDeleteModal = () => {
    if (!wishQuery.data) return;
    setWishToDelete(wishQuery.data.wish_id);
    deleteDialog.onOpen();
  };

  if (wishQuery.isLoading) {
    return (
      <Flex justify="center" align="center" minH="400px">
        <Spinner size="xl" />
      </Flex>
    );
  }

  if (wishQuery.isError || !wishQuery.data) {
    return (
      <Card>
        <CardBody>
          <Text color="red.500">Не удалось загрузить желание</Text>
          <Button mt={4} onClick={() => navigate(-1)}>
            Назад
          </Button>
        </CardBody>
      </Card>
    );
  }

  const wish = wishQuery.data;
  const isOwnWish = user?.user_id === wish.owner_id;

  return (
    <Card borderRadius="2xl" shadow="xl">
      <CardBody>
        <Button mb={4} variant="ghost" onClick={() => navigate(-1)}>
          ← Назад
        </Button>

        <Flex direction={{ base: "column", md: "row" }} gap={6}>
          <Box flex="0 0 300px">
            {wish.photo_url ? (
              <Image
                src={wish.photo_url}
                alt={wish.title}
                borderRadius="lg"
                objectFit="cover"
                w="100%"
                maxH="400px"
              />
            ) : (
              <Box
                border="1px dashed"
                borderColor="gray.300"
                borderRadius="lg"
                h="400px"
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
            <Heading size="lg" mb={2}>
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
            <Stack spacing={4} mb={6}>
              <Box>
                <Text fontSize="sm" color="gray.500" mb={1}>
                  Цена
                </Text>
                <Text fontWeight="semibold" fontSize="lg">
                  {wish.price ? `${wish.price} ₽` : "—"}
                </Text>
              </Box>
              <Divider />
              <Box>
                <Text fontSize="sm" color="gray.500" mb={2}>
                  Описание
                </Text>
                <Text color="gray.700" fontSize="md">
                  {wish.description || "Описание отсутствует"}
                </Text>
              </Box>
            </Stack>
            <Flex gap={3} direction={{ base: "column", md: "row" }} mt={6}>
              {isOwnWish ? (
                <>
                  <Button
                    colorScheme="purple"
                    size="lg"
                    flex={{ base: "1", md: "0 0 auto" }}
                    onClick={openEditModal}
                  >
                    Изменить
                  </Button>
                  <Button
                    colorScheme="red"
                    size="lg"
                    variant="outline"
                    flex={{ base: "1", md: "0 0 auto" }}
                    onClick={openDeleteModal}
                  >
                    Удалить
                  </Button>
                </>
              ) : (
                <Button
                  colorScheme="purple"
                  size="lg"
                  width={{ base: "full", md: "auto" }}
                  onClick={() => copyMutation.mutate()}
                  isLoading={copyMutation.isPending}
                  loadingText="Добавление..."
                >
                  Добавить к себе
                </Button>
              )}
            </Flex>
          </Box>
        </Flex>
      </CardBody>

      <EditWishModal
        isOpen={editModal.isOpen}
        form={editForm}
        onChange={handleFormChange}
        onSubmit={(event) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
        onClose={editModal.onClose}
        isLoading={updateMutation.isPending}
        hasError={Boolean(updateMutation.isError)}
      />

      <DeleteWishDialog
        isOpen={deleteDialog.isOpen}
        wish={wishQuery.data && wishToDelete === wishQuery.data.wish_id ? wishQuery.data : null}
        onCancel={deleteDialog.onClose}
        onConfirm={() => deleteMutation.mutate()}
        isLoading={deleteMutation.isPending}
      />
    </Card>
  );
};

