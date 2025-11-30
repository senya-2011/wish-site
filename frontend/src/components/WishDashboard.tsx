import { Box, Button, Container, Flex, Heading, Text } from "@chakra-ui/react";
import type { UserResponse } from "../api";
import { MyWishesPage } from "./MyWishesPage";

type WishDashboardProps = {
  user: UserResponse | null;
  onLogout: () => void;
};

export const WishDashboard = ({ user, onLogout }: WishDashboardProps) => (
  <Box bg="gray.50" minH="100vh" py={10}>
    <Container maxW="5xl">
      <Flex
        align={{ base: "flex-start", md: "center" }}
        justify="space-between"
        direction={{ base: "column", md: "row" }}
        mb={10}
        gap={4}
      >
        <Box>
          <Heading size="lg" mb={2}>
            Мои желания
          </Heading>
          <Text color="gray.600">
            Управляйте своим списком желаний, создавайте новые и следите за прогрессом.
          </Text>
        </Box>
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

      <MyWishesPage key={user?.user_id ?? "guest"} />
    </Container>
  </Box>
);

