import { Box, Button, Container, Flex, Link, Text } from "@chakra-ui/react";
import { Link as RouterLink, Outlet, useLocation } from "react-router-dom";
import type { UserResponse } from "../api";

type WishDashboardProps = {
  user: UserResponse | null;
  onLogout: () => void;
};

export const WishDashboard = ({ user, onLogout }: WishDashboardProps) => {
  const location = useLocation();
  const isMyWishes = location.pathname === "/my-wishes" || location.pathname === "/";
  const isSearch = location.pathname === "/search";

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
          </Flex>
          <Flex
            align="center"
            gap={4}
            bg="white"
            px={4}
            py={2}
            borderRadius="lg"
            shadow="md"
          >
            <Text color="gray.600">
              Вошли как <strong>{user?.name ?? "неизвестно"}</strong>
            </Text>
            <Button size="sm" variant="outline" onClick={onLogout}>
              Выйти
            </Button>
          </Flex>
        </Flex>

        <Outlet />
      </Container>
    </Box>
  );
};

