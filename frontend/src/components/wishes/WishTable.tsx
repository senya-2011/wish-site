import {
  Alert,
  AlertIcon,
  Badge,
  Button,
  ButtonGroup,
  Flex,
  IconButton,
  Skeleton,
  Table,
  Tbody,
  Td,
  Text,
  Th,
  Thead,
  Tr,
} from "@chakra-ui/react";
import { DeleteIcon } from "@chakra-ui/icons";
import type { MouseEvent } from "react";
import type { WishResponse } from "../../api";
import type { WishListHandlers } from "./types";

type WishTableProps = {
  items: WishResponse[];
  isLoading: boolean;
  isError: boolean;
  isEmpty: boolean;
  page: number;
  totalPages: number;
  onPrevPage: () => void;
  onNextPage: () => void;
} & WishListHandlers;

export const WishTable = ({
  items,
  isLoading,
  isError,
  isEmpty,
  page,
  totalPages,
  onPrevPage,
  onNextPage,
  onSelect,
  onEdit,
  onDelete,
}: WishTableProps) => {
  if (isLoading) {
    return (
      <Flex direction="column" gap={4}>
        {Array.from({ length: 4 }).map((_, index) => (
          <Skeleton key={index} height="64px" borderRadius="md" />
        ))}
      </Flex>
    );
  }

  if (isError) {
    return (
      <Alert status="error">
        <AlertIcon />
        Не удалось загрузить желания. Попробуйте позже.
      </Alert>
    );
  }

  if (isEmpty) {
    return (
      <Flex direction="column" align="center" py={10} gap={2}>
        <Text fontSize="lg" fontWeight="bold">
          Здесь пока пусто
        </Text>
        <Text color="gray.500">Добавьте первое желание с помощью кнопки выше.</Text>
      </Flex>
    );
  }

  return (
    <>
      <Table variant="simple">
        <Thead>
          <Tr>
            <Th>ID</Th>
            <Th>Название</Th>
            <Th>Цена</Th>
            <Th>Создано</Th>
            <Th textAlign="right">Действия</Th>
          </Tr>
        </Thead>
        <Tbody>
          {items.map((wish) => (
            <Tr
              key={wish.wish_id}
              _hover={{ bg: "gray.50", cursor: "pointer" }}
              onClick={() => onSelect(wish)}
            >
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
              <Td textAlign="right" verticalAlign="top" whiteSpace="nowrap">
                <Button
                  size="sm"
                  colorScheme="purple"
                  variant="outline"
                  mr={2}
                  onClick={(event: MouseEvent<HTMLButtonElement>) => {
                    event.stopPropagation();
                    onEdit(wish);
                  }}
                >
                  Изменить
                </Button>
                <IconButton
                  aria-label="Удалить желание"
                  size="sm"
                  colorScheme="red"
                  variant="outline"
                  icon={<DeleteIcon />}
                  onClick={(event: MouseEvent<HTMLButtonElement>) => {
                    event.stopPropagation();
                    onDelete(wish);
                  }}
                />
              </Td>
            </Tr>
          ))}
        </Tbody>
      </Table>

      <Flex justify="space-between" align="center" mt={6} direction={{ base: "column", md: "row" }} gap={4}>
        <Text color="gray.600">
          Страница {page} из {totalPages}
        </Text>
        <ButtonGroup>
          <Button onClick={onPrevPage} isDisabled={page === 1}>
            Назад
          </Button>
          <Button onClick={onNextPage} isDisabled={page >= totalPages}>
            Вперёд
          </Button>
        </ButtonGroup>
      </Flex>
    </>
  );
};

