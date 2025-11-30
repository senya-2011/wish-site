import {
  Alert,
  AlertIcon,
  Button,
  FormControl,
  FormLabel,
  Input,
  Modal,
  ModalBody,
  ModalCloseButton,
  ModalContent,
  ModalFooter,
  ModalHeader,
  ModalOverlay,
  Stack,
  Textarea,
} from "@chakra-ui/react";
import type { ChangeEvent } from "react";
import type { WishFormState } from "./types";

type CreateWishModalProps = {
  isOpen: boolean;
  form: WishFormState;
  onChange: (field: keyof WishFormState, value: string) => void;
  onSubmit: (event: React.FormEvent<HTMLFormElement>) => void;
  onClose: () => void;
  isLoading: boolean;
  hasError: boolean;
};

export const CreateWishModal = ({
  isOpen,
  form,
  onChange,
  onSubmit,
  onClose,
  isLoading,
  hasError,
}: CreateWishModalProps) => (
  <Modal isOpen={isOpen} onClose={onClose} size="lg">
    <ModalOverlay />
    <ModalContent as="form" onSubmit={onSubmit}>
      <ModalHeader>Новое желание</ModalHeader>
      <ModalCloseButton />
      <ModalBody>
        <Stack spacing={4}>
          <FormControl isRequired>
            <FormLabel>Название</FormLabel>
            <Input
              value={form.title}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onChange("title", event.target.value)}
            />
          </FormControl>
          <FormControl>
            <FormLabel>Описание</FormLabel>
            <Textarea
              value={form.description}
              rows={4}
              onChange={(event: ChangeEvent<HTMLTextAreaElement>) => onChange("description", event.target.value)}
            />
          </FormControl>
          <FormControl>
            <FormLabel>Ссылка на фото</FormLabel>
            <Input
              value={form.photoUrl}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onChange("photoUrl", event.target.value)}
            />
          </FormControl>
          <FormControl>
            <FormLabel>Цена (₽)</FormLabel>
            <Input
              type="number"
              step="0.01"
              value={form.price}
              onChange={(event: ChangeEvent<HTMLInputElement>) => onChange("price", event.target.value)}
            />
          </FormControl>
          {hasError && (
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
        <Button colorScheme="purple" type="submit" isLoading={isLoading} isDisabled={!form.title}>
          Создать
        </Button>
      </ModalFooter>
    </ModalContent>
  </Modal>
);

