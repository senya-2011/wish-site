import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  Flex,
  Heading,
  Text,
  useDisclosure,
} from "@chakra-ui/react";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import type { WishPageResponse, WishRequest, WishResponse, WishUpdateRequest } from "../../api";
import { wishesApi } from "../../lib/api-client";
import { useAuth } from "../../context/useAuth";
import type { WishFormState } from "./types";
import { WishTable } from "./WishTable";
import { CreateWishModal } from "./CreateWishModal";
import { EditWishModal } from "./EditWishModal";
import { WishDetailsModal } from "./WishDetailsModal";
import { DeleteWishDialog } from "./DeleteWishDialog";

const PAGE_SIZE = 10;
const getDefaultFormState = (): WishFormState => ({
  title: "",
  description: "",
  photoUrl: "",
  price: "",
  photoFile: null,
});

export const MyWishesPage = () => {
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const [page, setPage] = useState(1);
  const [createForm, setCreateForm] = useState<WishFormState>(getDefaultFormState);
  const [editForm, setEditForm] = useState<WishFormState>(getDefaultFormState);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [detailWish, setDetailWish] = useState<WishResponse | null>(null);
  const [wishToDelete, setWishToDelete] = useState<WishResponse | null>(null);

  const createModal = useDisclosure();
  const editModal = useDisclosure();
  const detailModal = useDisclosure();
  const deleteDialog = useDisclosure();

  const userId = user?.user_id;

  const wishesQuery = useQuery<WishPageResponse>({
    queryKey: ["my-wishes", userId, page],
    queryFn: async () => {
      if (!userId) throw new Error("Пользователь не найден");
      const response = await wishesApi.getUserWishes(userId, page - 1, PAGE_SIZE);
      return response.data;
    },
    enabled: Boolean(userId),
    placeholderData: keepPreviousData,
  });

  const totalPages = Math.max(1, wishesQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const items = wishesQuery.data?.items ?? [];

  const handleFormChange = (setter: React.Dispatch<React.SetStateAction<WishFormState>>) => (field: keyof WishFormState, value: string | File | null) =>
    setter((prev: WishFormState) => ({ ...prev, [field]: value }));

  const createMutation = useMutation({
    mutationFn: async () => {
      if (!userId) throw new Error("Пользователь не найден");
      
      if (createForm.photoFile) {
        const response = await wishesApi.createWishWithFile(
          userId,
          createForm.title,
          createForm.description || undefined,
          createForm.photoFile,
          createForm.price ? Number(createForm.price) : undefined
        );
        return response.data;
      } else {
        const payload: WishRequest = {
          title: createForm.title,
          description: createForm.description || undefined,
          photo_url: createForm.photoUrl || undefined,
          price: createForm.price ? Number(createForm.price) : undefined,
        };
        const response = await wishesApi.createWish(userId, payload);
        return response.data;
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      setCreateForm(getDefaultFormState());
      setPage(1);
      createModal.onClose();
    },
  });

  const updateMutation = useMutation({
    mutationFn: async () => {
      if (!editingId) throw new Error("Желание не выбрано");
      
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
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      editModal.onClose();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async () => {
      if (!wishToDelete) throw new Error("Желание не выбрано");
      await wishesApi.deleteWishById(wishToDelete.wish_id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["my-wishes"] });
      deleteDialog.onClose();
    },
  });

  const openEditModal = (wish: WishResponse) => {
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

  const openDeleteModal = (wish: WishResponse) => {
    setWishToDelete(wish);
    deleteDialog.onOpen();
  };

  const openDetailModal = (wish: WishResponse) => {
    setDetailWish(wish);
    detailModal.onOpen();
  };

  return (
    <>
      <Card borderRadius="2xl" shadow="xl">
        <CardHeader>
          <Flex justify="space-between" align={{ base: "stretch", md: "center" }} gap={4} direction={{ base: "column", md: "row" }}>
            <Box>
              <Heading size="md">Мои желания</Heading>
              <Text color="gray.500">Все желания, созданные вами. По 10 штук на страницу.</Text>
            </Box>
            <Button colorScheme="purple" onClick={createModal.onOpen}>
              Создать желание
            </Button>
          </Flex>
        </CardHeader>
        <CardBody>
          <WishTable
            items={items}
            isLoading={wishesQuery.isLoading}
            isError={Boolean(wishesQuery.isError)}
            isEmpty={!wishesQuery.isLoading && items.length === 0}
            page={safePage}
            totalPages={totalPages}
            onPrevPage={() => setPage((prev) => Math.max(1, prev - 1))}
            onNextPage={() => setPage((prev) => Math.min(totalPages, prev + 1))}
            onSelect={openDetailModal}
            onEdit={openEditModal}
            onDelete={openDeleteModal}
          />
        </CardBody>
      </Card>

      <CreateWishModal
        isOpen={createModal.isOpen}
        form={createForm}
        onChange={handleFormChange(setCreateForm)}
        onSubmit={(event) => {
          event.preventDefault();
          createMutation.mutate();
        }}
        onClose={createModal.onClose}
        isLoading={createMutation.isPending}
        hasError={Boolean(createMutation.isError)}
      />

      <EditWishModal
        isOpen={editModal.isOpen}
        form={editForm}
        onChange={handleFormChange(setEditForm)}
        onSubmit={(event) => {
          event.preventDefault();
          updateMutation.mutate();
        }}
        onClose={editModal.onClose}
        isLoading={updateMutation.isPending}
        hasError={Boolean(updateMutation.isError)}
      />

      <WishDetailsModal wish={detailWish} isOpen={detailModal.isOpen} onClose={detailModal.onClose} onEdit={openEditModal} />

      <DeleteWishDialog
        isOpen={deleteDialog.isOpen}
        wish={wishToDelete}
        onCancel={deleteDialog.onClose}
        onConfirm={() => deleteMutation.mutate()}
        isLoading={deleteMutation.isPending}
      />
    </>
  );
};

