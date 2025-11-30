import "./App.css";
import { UserList } from "./components/UserList";
import { CreateUserForm } from "./components/CreateUserForm";
import { GetUserById } from "./components/GetUserById";
import { UpdateUserForm } from "./components/UpdateUserForm";
import { DeleteUserForm } from "./components/DeleteUserForm";
import { GetUserWishes } from "./components/GetUserWishes";
import { CreateWishForm } from "./components/CreateWishForm";
import { GetWishById } from "./components/GetWishById";
import { UpdateWishForm } from "./components/UpdateWishForm";
import { DeleteWishForm } from "./components/DeleteWishForm";
import { AuthPage } from "./components/AuthPage";
import { useAuth } from "./context/useAuth";
import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  Container,
  Flex,
  Heading,
  SimpleGrid,
  Stack,
  Text,
} from "@chakra-ui/react";

const SectionCard = ({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) => (
  <Card borderRadius="2xl" shadow="xl">
    <CardHeader borderBottom="1px solid" borderColor="gray.100">
      <Heading size="md">{title}</Heading>
    </CardHeader>
    <CardBody>
      <Stack spacing={6}>{children}</Stack>
    </CardBody>
  </Card>
);

function App() {
  const { token, user, logout } = useAuth();

  if (!token) {
    return <AuthPage />;
  }

  return (
    <Box bg="gray.50" minH="100vh" py={10}>
      <Container maxW="6xl">
        <Flex
          align={{ base: "flex-start", md: "center" }}
          justify="space-between"
          direction={{ base: "column", md: "row" }}
          mb={10}
          gap={4}
        >
          <Box>
            <Heading size="lg" mb={2}>
              Wish site playground
            </Heading>
            <Text color="gray.600">
              Управляй пользователями и их желаниями прямо из браузера
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
            <Button size="sm" variant="outline" onClick={logout}>
              Выйти
            </Button>
          </Flex>
        </Flex>

        <SimpleGrid columns={{ base: 1, md: 2 }} spacing={8}>
          <SectionCard title="Пользователи">
            <UserList />
            <CreateUserForm />
            <GetUserById />
            <UpdateUserForm />
            <DeleteUserForm />
          </SectionCard>

          <SectionCard title="Желания">
            <GetUserWishes />
            <CreateWishForm />
            <GetWishById />
            <UpdateWishForm />
            <DeleteWishForm />
          </SectionCard>
        </SimpleGrid>
      </Container>
    </Box>
  );
}

export default App;
