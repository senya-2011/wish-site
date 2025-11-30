import {
  AlertDialog,
  AlertDialogBody,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogOverlay,
  Button,
} from "@chakra-ui/react";
import { useRef } from "react";
import type { WishResponse } from "../../api";

type DeleteWishDialogProps = {
  isOpen: boolean;
  wish: WishResponse | null;
  onCancel: () => void;
  onConfirm: () => void;
  isLoading: boolean;
};

export const DeleteWishDialog = ({ isOpen, wish, onCancel, onConfirm, isLoading }: DeleteWishDialogProps) => {
  const cancelRef = useRef<HTMLButtonElement>(null);

  return (
    <AlertDialog isOpen={isOpen} leastDestructiveRef={cancelRef} onClose={onCancel}>
      <AlertDialogOverlay>
        <AlertDialogContent>
          <AlertDialogHeader fontSize="lg" fontWeight="bold">
            Удалить желание
          </AlertDialogHeader>

          <AlertDialogBody>
            Точно удалить «{wish?.title ?? "это желание"}»? Это действие необратимо.
          </AlertDialogBody>

          <AlertDialogFooter>
            <Button ref={cancelRef} onClick={onCancel}>
              Отмена
            </Button>
            <Button colorScheme="red" ml={3} onClick={onConfirm} isLoading={isLoading}>
              Удалить
            </Button>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialogOverlay>
    </AlertDialog>
  );
};

