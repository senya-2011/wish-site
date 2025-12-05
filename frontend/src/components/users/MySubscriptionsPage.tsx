import {
  Card,
  CardBody,
  CardHeader,
  Flex,
  Heading,
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
  Button,
  Box,
} from "@chakra-ui/react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { UserPageResponse, UserResponse } from "../../api";
import { usersApi } from "../../lib/api-client";
import { SubscribeButton } from "./SubscribeButton";

const PAGE_SIZE = 10;

export const MySubscriptionsPage = () => {
  const navigate = useNavigate();
  const [page, setPage] = useState(1);

  const subscriptionsQuery = useQuery<UserPageResponse>({
    queryKey: ["user-subscriptions", page],
    queryFn: async () => {
      const response = await usersApi.getMySubscriptions(page - 1, PAGE_SIZE);
      return response.data;
    },
    placeholderData: keepPreviousData,
  });

  const totalPages = Math.max(1, subscriptionsQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const items = subscriptionsQuery.data?.items ?? [];

  const handleSelect = (userId: number) => {
    navigate(`/users/${userId}/wishes`);
  };

  if (subscriptionsQuery.isLoading) {
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

  if (subscriptionsQuery.isError) {
    return (
      <Card borderRadius="2xl" shadow="xl">
        <CardBody>
          <Alert status="error">
            <AlertIcon />
            Не удалось загрузить подписки. Попробуйте позже.
          </Alert>
        </CardBody>
      </Card>
    );
  }

  return (
    <Card borderRadius="2xl" shadow="xl">
      <CardHeader>
        <Heading size="md">Мои подписки</Heading>
      </CardHeader>
      <CardBody>
        {items.length > 0 ? (
          <>
            <Box overflowX="auto">
              <Table variant="simple">
                <Thead>
                  <Tr>
                    <Th>Имя</Th>
                    <Th display={{ base: "none", md: "table-cell" }}>Роль</Th>
                    <Th display={{ base: "none", md: "table-cell" }}>Telegram</Th>
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
                      <Td display={{ base: "none", md: "table-cell" }}>
                        <Text color="gray.600" textTransform="capitalize">
                          {user.role}
                        </Text>
                      </Td>
                      <Td display={{ base: "none", md: "table-cell" }}>
                        <Text color="gray.600">
                          {user.telegram_username ? `@${user.telegram_username}` : "—"}
                        </Text>
                      </Td>
                      <Td textAlign="right" whiteSpace="nowrap">
                        <Flex gap={2} justify="flex-end" onClick={(e: React.MouseEvent) => e.stopPropagation()}>
                          <SubscribeButton userId={user.user_id} isSubscribed={true} />
                          <Button
                            size="sm"
                            colorScheme="purple"
                            variant="outline"
                            display={{ base: "none", md: "inline-flex" }}
                            onClick={(e: React.MouseEvent<HTMLButtonElement>) => {
                              e.stopPropagation();
                              handleSelect(user.user_id);
                            }}
                          >
                            Просмотреть желания
                          </Button>
                        </Flex>
                      </Td>
                    </Tr>
                  ))}
                </Tbody>
              </Table>
            </Box>

            <Flex justify="space-between" align="center" mt={6} direction={{ base: "column", md: "row" }} gap={4}>
              <Text color="gray.600">
                Страница {safePage} из {totalPages}
              </Text>
              <Flex gap={2}>
                <Button onClick={() => setPage((prev) => Math.max(1, prev - 1))} isDisabled={safePage === 1}>
                  Назад
                </Button>
                <Button
                  onClick={() => setPage((prev) => Math.min(totalPages, prev + 1))}
                  isDisabled={safePage >= totalPages}
                >
                  Вперёд
                </Button>
              </Flex>
            </Flex>
          </>
        ) : (
          <Flex direction="column" align="center" py={10} gap={2}>
            <Text fontSize="lg" fontWeight="bold">
              У вас пока нет подписок
            </Text>
            <Text color="gray.500">Найдите пользователей и подпишитесь на них, чтобы получать уведомления</Text>
          </Flex>
        )}
      </CardBody>
    </Card>
  );
};

