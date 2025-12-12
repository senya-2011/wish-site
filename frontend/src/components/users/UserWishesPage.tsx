import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  Flex,
  Heading,
  Spinner,
} from "@chakra-ui/react";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import type { WishPageResponse, WishResponse, UserPageResponse } from "../../api";
import { wishesApi, usersApi } from "../../lib/api-client";
import { WishTable } from "../wishes/WishTable";
import { SubscribeButton } from "./SubscribeButton";

const PAGE_SIZE = 10;

export const UserWishesPage = () => {
  const { userId } = useParams<{ userId: string }>();
  const navigate = useNavigate();
  const [page, setPage] = useState(1);

  const userQuery = useQuery({
    queryKey: ["user", userId],
    queryFn: async () => {
      if (!userId) throw new Error("User ID is required");
      const response = await usersApi.getUserById(Number(userId));
      return response.data;
    },
    enabled: Boolean(userId),
  });

  const subscriptionsQuery = useQuery<UserPageResponse>({
    queryKey: ["user-subscriptions"],
    queryFn: async () => {
      const response = await usersApi.getMySubscriptions(0, 50);
      return response.data;
    },
  });

  const wishesQuery = useQuery<WishPageResponse>({
    queryKey: ["user-wishes", userId, page],
    queryFn: async () => {
      if (!userId) throw new Error("User ID is required");
      const response = await wishesApi.getUserWishes(Number(userId), page - 1, PAGE_SIZE);
      return response.data;
    },
    enabled: Boolean(userId),
    placeholderData: keepPreviousData,
  });

  const subscribedUserIds = new Set(subscriptionsQuery.data?.items?.map((u) => u.user_id) || []);
  const isSubscribed = userId ? subscribedUserIds.has(Number(userId)) : false;

  const totalPages = Math.max(1, wishesQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const items = wishesQuery.data?.items ?? [];

  const handleSelect = (wish: WishResponse) => {
    navigate(`/wishes/${wish.wish_id}`);
  };

  return (
    <Card borderRadius="2xl" shadow="xl">
      <CardHeader>
        <Flex justify="space-between" align={{ base: "stretch", md: "center" }} gap={4} direction={{ base: "column", md: "row" }}>
          <Box>
            <Heading size="md">
              Желания пользователя: {userQuery.isLoading ? <Spinner size="sm" ml={2} /> : userQuery.data?.name || "Загрузка..."}
            </Heading>
          </Box>
          <Flex gap={2} align="center">
            {userId && <SubscribeButton userId={Number(userId)} isSubscribed={isSubscribed} />}
            <Button variant="ghost" onClick={() => navigate(-1)}>
              ← Назад
            </Button>
          </Flex>
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
          onSelect={handleSelect}
          showActions={false}
        />
      </CardBody>
    </Card>
  );
};

