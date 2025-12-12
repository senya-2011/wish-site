import { IconButton, Tooltip, useToast, Box } from "@chakra-ui/react";
import { BellIcon } from "@chakra-ui/icons";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { usersApi } from "../../lib/api-client";
import { useAuth } from "../../context/useAuth";
import { TelegramVerificationModal } from "./TelegramVerificationModal";
import { useState } from "react";

type SubscribeButtonProps = {
  userId: number;
  isSubscribed?: boolean;
  onSubscriptionChange?: () => void;
};

export const SubscribeButton = ({ userId, isSubscribed: initialIsSubscribed, onSubscriptionChange }: SubscribeButtonProps) => {
  const { user } = useAuth();
  const toast = useToast();
  const queryClient = useQueryClient();
  const [showTelegramModal, setShowTelegramModal] = useState(false);

  const subscriptionsQuery = useQuery({
    queryKey: ["user-subscriptions"],
    queryFn: async () => {
      const response = await usersApi.getMySubscriptions(0, 50);
      return response.data;
    },
    enabled: Boolean(user),
    staleTime: 30000, // Кешируем на 30 секунд
  });

  const subscribedUserIds = new Set(subscriptionsQuery.data?.items?.map((u) => u.user_id) || []);
  const isSubscribed = initialIsSubscribed !== undefined ? initialIsSubscribed : subscribedUserIds.has(userId);

  const subscribeMutation = useMutation({
    mutationFn: async () => {
      if (!user?.telegram_username) {
        setShowTelegramModal(true);
        return;
      }
      return usersApi.subscribeToUser(userId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-subscriptions"] });
      queryClient.invalidateQueries({ queryKey: ["user", userId] });
      onSubscriptionChange?.();
      toast({
        title: "Подписка оформлена",
        description: "Вы будете получать уведомления о новых желаниях",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
    },
    onError: (error: unknown) => {
      const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || "Не удалось оформить подписку";
      if (message.includes("Telegram")) {
        setShowTelegramModal(true);
      } else {
        toast({
          title: "Ошибка",
          description: message,
          status: "error",
          duration: 3000,
          isClosable: true,
        });
      }
    },
  });

  const unsubscribeMutation = useMutation({
    mutationFn: () => usersApi.unsubscribeFromUser(userId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-subscriptions"] });
      queryClient.invalidateQueries({ queryKey: ["user", userId] });
      onSubscriptionChange?.();
      toast({
        title: "Подписка отменена",
        status: "info",
        duration: 3000,
        isClosable: true,
      });
    },
    onError: (error: unknown) => {
      const message = (error as { response?: { data?: { message?: string } } })?.response?.data?.message || "Не удалось отменить подписку";
      toast({
        title: "Ошибка",
        description: message,
        status: "error",
        duration: 3000,
        isClosable: true,
      });
    },
  });

  const handleClick = () => {
    if (isSubscribed) {
      unsubscribeMutation.mutate();
    } else {
      subscribeMutation.mutate();
    }
  };

  if (user?.user_id === userId) {
    return null;
  }

  return (
    <>
      <Tooltip label={isSubscribed ? "Отписаться" : "Подписаться"}>
        <IconButton
          aria-label={isSubscribed ? "Отписаться" : "Подписаться"}
          icon={<BellIcon />}
          onClick={handleClick}
          isLoading={subscribeMutation.isPending || unsubscribeMutation.isPending}
          colorScheme={isSubscribed ? "red" : "purple"}
          variant={isSubscribed ? "solid" : "outline"}
          size="sm"
          bg={isSubscribed ? "red.500" : undefined}
          color={isSubscribed ? "white" : undefined}
          _hover={isSubscribed ? { bg: "red.600" } : undefined}
          position="relative"
        >
          {isSubscribed && (
            <Box
              position="absolute"
              top="50%"
              left="50%"
              transform="translate(-50%, -50%) rotate(45deg)"
              width="2px"
              height="20px"
              bg="white"
              pointerEvents="none"
            />
          )}
        </IconButton>
      </Tooltip>
      {showTelegramModal && (
        <TelegramVerificationModal
          isOpen={showTelegramModal}
          onClose={() => setShowTelegramModal(false)}
          onSuccess={() => {
            setShowTelegramModal(false);
            subscribeMutation.mutate();
          }}
        />
      )}
    </>
  );
};

