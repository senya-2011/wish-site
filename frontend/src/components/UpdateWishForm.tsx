import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi } from "../lib/api-client";
import type { WishUpdateRequest } from "../api";

export const UpdateWishForm = () => {
  const [wishId, setWishId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [price, setPrice] = useState("");

  const mutation = useMutation({
    mutationFn: async (payload: { wishId: number; body: WishUpdateRequest }) => {
      const response = await wishesApi.updateWishById(
        payload.wishId,
        payload.body
      );
      return response.data;
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = Number(wishId);
    if (Number.isNaN(id)) {
      alert("Введите корректный ID желания");
      return;
    }

    const body: WishUpdateRequest = {};
    if (title) body.title = title;
    if (description) body.description = description;
    if (photoUrl) body.photo_url = photoUrl;
    if (price) body.price = Number(price);

    if (Object.keys(body).length === 0) {
      alert("Укажите хотя бы одно поле для обновления");
      return;
    }

    mutation.mutate({ wishId: id, body });
  };

  return (
    <div className="card">
      <h3>Обновить желание</h3>
      <form onSubmit={handleSubmit} className="form">
        <input
          type="number"
          min={1}
          placeholder="ID желания"
          value={wishId}
          onChange={(e) => setWishId(e.target.value)}
          className="form-input"
          required
        />
        <input
          placeholder="Новый заголовок"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="form-input"
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
          placeholder="Новая цена"
          value={price}
          onChange={(e) => setPrice(e.target.value)}
          className="form-input"
        />
        <textarea
          placeholder="Новое описание"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          className="form-textarea"
        />
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Сохраняем..." : "Обновить желание"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при обновлении желания</p>
      )}

      {mutation.isSuccess && (
        <pre className="response-box">
          {JSON.stringify(mutation.data, null, 2)}
        </pre>
      )}
    </div>
  );
};

