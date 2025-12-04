import {
  Modal,
  ModalOverlay,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalCloseButton,
  Button,
  Input,
  VStack,
  Text,
  Alert,
  AlertIcon,
  InputGroup,
  InputRightElement,
  IconButton,
  Link,
} from "@chakra-ui/react";
import { ArrowForwardIcon } from "@chakra-ui/icons";
import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { useAuth } from "../../context/useAuth";
import { useQueryClient } from "@tanstack/react-query";
import { usersApi } from "../../lib/api-client";

type TelegramVerificationModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onSuccess?: () => void;
};

type Step = "username" | "code";

export const TelegramVerificationModal = ({ isOpen, onClose, onSuccess }: TelegramVerificationModalProps) => {
  const [step, setStep] = useState<Step>("username");
  const [telegramUsername, setTelegramUsername] = useState("");
  const [verificationCode, setVerificationCode] = useState("");
  const { user } = useAuth();
  const queryClient = useQueryClient();

  const verifyMutation = useMutation({
    mutationFn: (username: string) =>
      usersApi.verifyTelegram({
        telegram_username: username,
      }),
    onSuccess: () => {
      setStep("code");
    },
    onError: (error: unknown) => {
      console.error("Verify error:", error);
    },
  });

  const { refreshUser } = useAuth();

  const confirmMutation = useMutation({
    mutationFn: (code: string) =>
      usersApi.confirmTelegram({
        verification_code: code,
      }),
    onSuccess: async () => {
      onClose();
      
      try {
        queryClient.invalidateQueries({ queryKey: ["user", user?.user_id] });
        queryClient.invalidateQueries({ queryKey: ["auth"] });
        if (refreshUser) {
          await refreshUser();
        }
        onSuccess?.();
      } catch (error) {
        console.error("Error refreshing user after Telegram confirmation:", error);
      }
      
      setStep("username");
      setTelegramUsername("");
      setVerificationCode("");
    },
    onError: (error: unknown) => {
      console.error("Confirm error:", error);
    },
  });

  const handleClose = () => {
    setStep("username");
    setTelegramUsername("");
    setVerificationCode("");
    onClose();
  };

  const handleUsernameSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (telegramUsername.trim()) {
      verifyMutation.mutate(telegramUsername.trim().replace(/^@/, ""));
    }
  };

  const handleCodeSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (verificationCode.trim()) {
      confirmMutation.mutate(verificationCode.trim());
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} isCentered>
      <ModalOverlay />
      <ModalContent>
        <ModalHeader>Привязка Telegram</ModalHeader>
        <ModalCloseButton />
        <ModalBody pb={6}>
          {step === "username" ? (
            <VStack spacing={4} align="stretch">
              <Text>Введите ваш Telegram username для получения уведомлений:</Text>
              <form onSubmit={handleUsernameSubmit}>
                <InputGroup>
                  <Input
                    placeholder="@username"
                    value={telegramUsername}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setTelegramUsername(e.target.value)}
                    isDisabled={verifyMutation.isPending}
                  />
                  <InputRightElement>
                    <IconButton
                      aria-label="Отправить"
                      icon={<ArrowForwardIcon />}
                      type="submit"
                      isLoading={verifyMutation.isPending}
                      size="sm"
                      colorScheme="purple"
                    />
                  </InputRightElement>
                </InputGroup>
              </form>
              {verifyMutation.isError && (
                <Alert status="error">
                  <AlertIcon />
                  {(verifyMutation.error as any)?.response?.data?.message || "Ошибка при отправке кода"}
                </Alert>
              )}
              {verifyMutation.isSuccess && (
                <Alert status="success">
                  <AlertIcon />
                  Код отправлен в Telegram бот. Проверьте сообщения.
                </Alert>
              )}
            </VStack>
          ) : (
            <VStack spacing={4} align="stretch">
              <Text>
                Введите код из{" "}
                <Link href="https://t.me/dabwish_bot" isExternal color="blue.500">
                  t.me/dabwish_bot
                </Link>
                :
              </Text>
              <form onSubmit={handleCodeSubmit}>
                <InputGroup>
                  <Input
                    placeholder="123456"
                    value={verificationCode}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setVerificationCode(e.target.value.replace(/\D/g, ""))}
                    maxLength={6}
                    isDisabled={confirmMutation.isPending}
                  />
                  <InputRightElement>
                    <IconButton
                      aria-label="Подтвердить"
                      icon={<ArrowForwardIcon />}
                      type="submit"
                      isLoading={confirmMutation.isPending}
                      size="sm"
                      colorScheme="purple"
                    />
                  </InputRightElement>
                </InputGroup>
              </form>
              {confirmMutation.isError && (
                <Alert status="error">
                  <AlertIcon />
                  {(confirmMutation.error as any)?.response?.data?.message || "Неверный код"}
                </Alert>
              )}
              <Button variant="ghost" size="sm" onClick={() => setStep("username")}>
                ← Назад
              </Button>
            </VStack>
          )}
        </ModalBody>
      </ModalContent>
    </Modal>
  );
};

