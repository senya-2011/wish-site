import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi } from "../lib/api-client";
import type { WishRequest } from "../api";

export const CreateWishForm = () => {
  const [userId, setUserId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [price, setPrice] = useState("");

  const queryClient = useQueryClient();

  const mutation = useMutation({
    mutationFn: async (payload: { userId: number; body: WishRequest }) => {
      const response = await wishesApi.createWish(payload.userId, payload.body);
      return response.data;
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setTitle("");
      setDescription("");
      setPhotoUrl("");
      setPrice("");
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = Number(userId);
    if (Number.isNaN(id)) {
      alert("Введите корректный ID пользователя");
      return;
    }

    const body: WishRequest = {
      title,
      description: description || undefined,
      photo_url: photoUrl || undefined,
      price: price ? Number(price) : undefined,
    };

    mutation.mutate({ userId: id, body });
  };

  return (
    <div className="card">
      <h3>Создать желание</h3>
      <form onSubmit={handleSubmit} className="form">
        <input
          type="number"
          min={1}
          placeholder="ID пользователя"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          className="form-input"
          required
        />
        <input
          placeholder="Заголовок"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="form-input"
          required
        />
        <input
          placeholder="Ссылка на фото"
          value={photoUrl}
          onChange={(e) => setPhotoUrl(e.target.value)}
          className="form-input"
        />
        <input
          type="number"
          step="0.01"
          placeholder="Цена"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          className="form-input"
        />
        <textarea
          placeholder="Описание"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="form-textarea"
        />
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Создаем..." : "Создать желание"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при создании желания</p>
      )}

      {mutation.isSuccess && (
        <pre className="response-box">
          {JSON.stringify(mutation.data, null, 2)}
        </pre>
      )}
    </div>
  );
};

