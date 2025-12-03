import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Flex,
  Heading,
  Input,
  InputGroup,
  InputLeftElement,
  Table,
  Tbody,
  Td,
  Text,
  Th,
  Thead,
  Tr,
  Skeleton,
  Alert,
  AlertIcon,
} from "@chakra-ui/react";
import { SearchIcon } from "@chakra-ui/icons";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { UserPageResponse, UserResponse } from "../../api";
import { usersApi } from "../../lib/api-client";

const PAGE_SIZE = 10;

export const UserSearchPage = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(1);

  const usersQuery = useQuery<UserPageResponse>({
    queryKey: ["search-users", searchQuery, page],
    queryFn: async () => {
      if (!searchQuery.trim()) {
        return { items: [], page: 0, size: 0, total_elements: 0, total_pages: 0 };
      }
      const response = await usersApi.searchUsers(searchQuery, page - 1, PAGE_SIZE);
      return response.data;
    },
    enabled: searchQuery.trim().length > 0,
    placeholderData: keepPreviousData,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchQuery(query);
    setPage(1);
  };

  const totalPages = Math.max(1, usersQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const items = usersQuery.data?.items ?? [];

  const handleSelect = (userId: number) => {
    navigate(`/users/${userId}/wishes`);
  };

  if (usersQuery.isLoading) {
    return (
      <Card borderRadius="2xl" shadow="xl">
        <CardBody>
          <Flex direction="column" gap={4}>
            {Array.from({ length: 4 }).map((_, index) => (
              <Skeleton key={index} height="64px" borderRadius="md" />
            ))}
          </Flex>
        </CardBody>
      </Card>
    );
  }

  if (usersQuery.isError) {
    return (
      <Card borderRadius="2xl" shadow="xl">
        <CardBody>
          <Alert status="error">
            <AlertIcon />
            Не удалось загрузить пользователей. Попробуйте позже.
          </Alert>
        </CardBody>
      </Card>
    );
  }

  return (
    <Card borderRadius="2xl" shadow="xl">
      <CardHeader>
        <Heading size="md">Поиск пользователей</Heading>
      </CardHeader>
      <CardBody>
        <form onSubmit={handleSearch}>
          <InputGroup size="lg" mb={6}>
            <InputLeftElement pointerEvents="none">
              <SearchIcon color="gray.300" />
            </InputLeftElement>
            <Input
              placeholder="Введите имя пользователя..."
              value={query}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)}
              bg="white"
            />
          </InputGroup>
          <Button type="submit" colorScheme="purple" width="full" mb={6}>
            Найти
          </Button>
        </form>

        {searchQuery && items.length > 0 && (
          <>
            <Table variant="simple">
              <Thead>
                <Tr>
                  <Th>Имя</Th>
                  <Th>Роль</Th>
                  <Th textAlign="right">Действия</Th>
                </Tr>
              </Thead>
              <Tbody>
                {items.map((user: UserResponse) => (
                  <Tr
                    key={user.user_id}
                    _hover={{ bg: "gray.50", cursor: "pointer" }}
                    onClick={() => handleSelect(user.user_id)}
                  >
                    <Td>
                      <Text fontWeight="semibold">{user.name}</Text>
                    </Td>
                    <Td>
                      <Text color="gray.600" textTransform="capitalize">{user.role}</Text>
                    </Td>
                    <Td textAlign="right" whiteSpace="nowrap">
                      <Button
                        size="sm"
                        colorScheme="purple"
                        variant="outline"
                        onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                          e.stopPropagation();
                          handleSelect(user.user_id);
                        }}
                      >
                        Просмотреть желания
                      </Button>
                    </Td>
                  </Tr>
                ))}
              </Tbody>
            </Table>

            <Flex justify="space-between" align="center" mt={6} direction={{ base: "column", md: "row" }} gap={4}>
              <Text color="gray.600">
                Страница {safePage} из {totalPages}
              </Text>
              <Flex gap={2}>
                <Button onClick={() => setPage((prev) => Math.max(1, prev - 1))} isDisabled={safePage === 1}>
                  Назад
                </Button>
                <Button onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))} isDisabled={safePage >= totalPages}>
                  Вперёд
                </Button>
              </Flex>
            </Flex>
          </>
        )}

        {searchQuery && !usersQuery.isLoading && items.length === 0 && (
          <Flex direction="column" align="center" py={10} gap={2}>
            <Text fontSize="lg" fontWeight="bold">
              Пользователи не найдены
            </Text>
            <Text color="gray.500">Попробуйте изменить поисковый запрос</Text>
          </Flex>
        )}

        {!searchQuery && (
          <Flex direction="column" align="center" py={10} gap={2}>
            <Text fontSize="lg" fontWeight="bold">
              Введите поисковый запрос
            </Text>
            <Text color="gray.500">Начните поиск, чтобы найти пользователей</Text>
          </Flex>
        )}
      </CardBody>
    </Card>
  );
};

