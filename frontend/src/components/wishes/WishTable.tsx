import {
  Alert,
  AlertIcon,
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
  showActions?: boolean;
} & Partial<WishListHandlers>;

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
  showActions = true,
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
      {/* Mobile view - только название */}
      <Flex direction="column" gap={3} display={{ base: "flex", md: "none" }}>
        {items.map((wish) => (
          <Flex
            key={wish.wish_id}
            p={4}
            bg="white"
            borderRadius="md"
            border="1px"
            borderColor="gray.200"
            _hover={{ bg: "gray.50", cursor: "pointer", borderColor: "purple.300" }}
            onClick={() => onSelect?.(wish)}
            justify="space-between"
            align="center"
            gap={2}
          >
            <Text fontWeight="semibold" fontSize="md" flex="1">
              {wish.title}
            </Text>
            {showActions && (
              <Flex gap={2} onClick={(e: React.MouseEvent) => e.stopPropagation()}>
                {onEdit && (
                  <Button
                    size="sm"
                    colorScheme="purple"
                    variant="outline"
                    onClick={(event: MouseEvent<HTMLButtonElement>) => {
                      event.stopPropagation();
                      onEdit(wish);
                    }}
                  >
                    Изменить
                  </Button>
                )}
                {onDelete && (
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
                )}
              </Flex>
            )}
          </Flex>
        ))}
      </Flex>

      {/* Desktop view - полная таблица */}
      <Table variant="simple" display={{ base: "none", md: "table" }}>
        <Thead>
          <Tr>
            <Th>Название</Th>
            <Th>Цена</Th>
            <Th>Создано</Th>
            {showActions && <Th textAlign="right">Действия</Th>}
          </Tr>
        </Thead>
        <Tbody>
          {items.map((wish) => (
            <Tr
              key={wish.wish_id}
              _hover={{ bg: "gray.50", cursor: "pointer" }}
              onClick={() => onSelect?.(wish)}
            >
              <Td>
                <Text fontWeight="semibold">{wish.title}</Text>
                {wish.description && (
                  <Text color="gray.500" fontSize="sm" noOfLines={2}>
                    {wish.description}
                  </Text>
                )}
              </Td>
              <Td whiteSpace="nowrap">{wish.price ? `${wish.price} ₽` : "—"}</Td>
              <Td whiteSpace="nowrap">
                <Text fontSize="sm">
                  {new Date(wish.created_at).toLocaleDateString("ru-RU", {
                    day: "numeric",
                    month: "long",
                    year: "numeric"
                  })}
                </Text>
                <Text fontSize="xs" color="gray.500">
                  {new Date(wish.created_at).toLocaleTimeString("ru-RU", {
                    hour: "2-digit",
                    minute: "2-digit"
                  })}
                </Text>
              </Td>
              {showActions && (
                <Td textAlign="right" verticalAlign="top" whiteSpace="nowrap">
                  {onEdit && (
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
                  )}
                  {onDelete && (
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
                  )}
                </Td>
              )}
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

