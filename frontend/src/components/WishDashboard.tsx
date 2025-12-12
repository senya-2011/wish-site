import { Box, Button, Container, Flex, Link, Text, Badge, Tooltip } from "@chakra-ui/react";
import { Link as RouterLink, Outlet, useLocation } from "react-router-dom";
import type { UserResponse } from "../api";
import { TelegramVerificationModal } from "./users/TelegramVerificationModal";
import { useState } from "react";
import { useAuth } from "../context/useAuth";

type WishDashboardProps = {
  user: UserResponse | null;
  onLogout: () => void;
};

export const WishDashboard = ({ user: propUser, onLogout }: WishDashboardProps) => {
  const { user: authUser, refreshUser } = useAuth();
  const user = authUser || propUser;
  const location = useLocation();
  const isMyWishes = location.pathname === "/my-wishes" || location.pathname === "/";
  const isSearch = location.pathname === "/search";
  const isSubscriptions = location.pathname === "/subscriptions";
  const [showTelegramModal, setShowTelegramModal] = useState(false);

  return (
    <Box bg="gray.50" minH="100vh" py={10}>
      <Container maxW="5xl">
        <Flex
          align={{ base: "flex-start", md: "center" }}
          justify="space-between"
          direction={{ base: "column", md: "row" }}
          mb={10}
          gap={4}
        >
          <Flex gap={6} align="center" flexWrap="wrap">
            <Link
              as={RouterLink}
              to="/my-wishes"
              fontSize="2xl"
              fontWeight="bold"
              color={isMyWishes ? "purple.500" : "gray.700"}
              _hover={{ color: "purple.600", textDecoration: "none" }}
            >
              Мои желания
            </Link>
            <Link
              as={RouterLink}
              to="/search"
              fontSize="2xl"
              fontWeight="bold"
              color={isSearch ? "purple.500" : "gray.700"}
              _hover={{ color: "purple.600", textDecoration: "none" }}
            >
              Поиск
            </Link>
            <Link
              as={RouterLink}
              to="/subscriptions"
              fontSize="2xl"
              fontWeight="bold"
              color={isSubscriptions ? "purple.500" : "gray.700"}
              _hover={{ color: "purple.600", textDecoration: "none" }}
            >
              Подписки
            </Link>
          </Flex>
          <Flex
            align="center"
            gap={4}
            bg="white"
            px={4}
            py={2}
            borderRadius="lg"
            shadow="md"
            flexWrap="wrap"
          >
            <Flex align="center" gap={2} flexWrap="wrap">
              <Text color="gray.600">
                Вошли как <strong>{user?.name ?? "неизвестно"}</strong>
              </Text>
              {user?.telegram_username ? (
                <Tooltip label={`Вы привязали телеграм: @${user.telegram_username}`}>
                  <Badge colorScheme="green" cursor="default">
                    Telegram: @{user.telegram_username}
                  </Badge>
                </Tooltip>
              ) : (
                <Tooltip label="Привязать Telegram для уведомлений">
                  <Badge colorScheme="gray" cursor="pointer" onClick={() => setShowTelegramModal(true)}>
                    Telegram не привязан
                  </Badge>
                </Tooltip>
              )}
            </Flex>
            <Button size="sm" variant="outline" onClick={onLogout}>
              Выйти
            </Button>
          </Flex>
        </Flex>

        <Outlet />
      </Container>
      {showTelegramModal && (
        <TelegramVerificationModal
          isOpen={showTelegramModal}
          onClose={() => setShowTelegramModal(false)}
          onSuccess={async () => {
            setShowTelegramModal(false);
            if (refreshUser) {
              await refreshUser();
            }
          }}
        />
      )}
    </Box>
  );
};

