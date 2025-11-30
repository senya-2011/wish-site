import { useState } from "react";
import type { ChangeEvent } from "react";
import { isAxiosError } from "axios";
import { useAuth } from "../context/AuthContext";
import type { LoginRequest, RegisterRequest } from "../api";
import {
  Box,
  Button,
  Card,
  CardBody,
  CardHeader,
  Flex,
  FormControl,
  FormLabel,
  Heading,
  Input,
  Link,
  Stack,
  Text,
} from "@chakra-ui/react";

type Mode = "login" | "register";

export const AuthPage = () => {
  const { login, register } = useAuth();
  const [mode, setMode] = useState<Mode>("login");
  const [name, setName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setIsSubmitting(true);
    try {
      if (mode === "login") {
        await login({ name, password } as LoginRequest);
      } else {
        await register({ name, password } as RegisterRequest);
      }
    } catch (err) {
      setError(extractMessage(err));
    } finally {
      setIsSubmitting(false);
    }
  };

  const extractMessage = (err: unknown): string => {
    if (isAxiosError(err)) {
      return (
        (err.response?.data as { message?: string } | undefined)?.message ??
        err.response?.statusText ??
        "Не удалось выполнить запрос"
      );
    }
    if (err instanceof Error) {
      return err.message;
    }
    return "Неизвестная ошибка";
  };

  const toggleMode = () => {
    setMode((prev) => (prev === "login" ? "register" : "login"));
    setError(null);
  };

  return (
    <Flex minH="100vh" align="center" justify="center" px={4}>
      <Card w="100%" maxW="420px" shadow="xl" borderRadius="2xl">
        <CardHeader textAlign="center">
          <Stack spacing={2}>
            <Heading size="lg">
              {mode === "login" ? "Вход в систему" : "Регистрация"}
            </Heading>
            <Text color="gray.500">
              {mode === "login"
                ? "Введите имя пользователя и пароль"
                : "Создайте новый аккаунт, чтобы продолжить"}
            </Text>
          </Stack>
        </CardHeader>
        <CardBody>
          <form onSubmit={handleSubmit}>
            <Stack spacing={4}>
              <FormControl isRequired>
                <FormLabel>Имя пользователя</FormLabel>
                <Input
                  value={name}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setName(e.target.value)
                  }
                  placeholder="Введите имя"
                />
              </FormControl>
              <FormControl isRequired>
                <FormLabel>Пароль</FormLabel>
                <Input
                  type="password"
                  value={password}
                  onChange={(e: ChangeEvent<HTMLInputElement>) =>
                    setPassword(e.target.value)
                  }
                  placeholder="Минимум 6 символов"
                  minLength={6}
                />
              </FormControl>
              {error && (
                <Box
                  color="red.500"
                  bg="red.50"
                  borderRadius="md"
                  p={2}
                  fontSize="sm"
                >
                  {error}
                </Box>
              )}
              <Button
                colorScheme="purple"
                type="submit"
                isLoading={isSubmitting}
              >
                {mode === "login" ? "Войти" : "Зарегистрироваться"}
              </Button>
            </Stack>
          </form>
          <Flex mt={4} align="center" justify="center" gap={2} fontSize="sm">
            <Text color="gray.600">
              {mode === "login"
                ? "Нет аккаунта?"
                : "Уже зарегистрированы?"}
            </Text>
            <Link color="purple.600" fontWeight="semibold" onClick={toggleMode}>
              {mode === "login" ? "Создать" : "Войти"}
            </Link>
          </Flex>
        </CardBody>
      </Card>
    </Flex>
  );
};

