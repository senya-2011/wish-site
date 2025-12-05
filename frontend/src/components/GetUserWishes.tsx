import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi } from "../lib/api-client";

export const GetUserWishes = () => {
  const [userId, setUserId] = useState("");

  const mutation = useMutation({
    mutationFn: async (id: number) => {
      const response = await wishesApi.getUserWishes(id);
      return response.data;
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const id = Number(userId);
    if (Number.isNaN(id)) {
      alert("Введите корректный ID пользователя");
      return;
    }
    mutation.mutate(id);
  };

  return (
    <div className="card">
      <h3>Желания пользователя</h3>
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
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Загружаем..." : "Показать желания"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при загрузке желаний</p>
      )}

      {mutation.isSuccess && mutation.data.items.length === 0 && (
        <p className="text-muted">У пользователя пока нет желаний</p>
      )}

      {mutation.isSuccess && mutation.data.items.length > 0 && (
        <ul className="simple-list">
          {mutation.data.items.map((wish) => (
            <li key={wish.wish_id}>
              <div>
                <div className="item-title">{wish.title}</div>
                <div className="text-muted">
                  Цена: {wish.price ? `${wish.price} ₽` : "—"} • 
                  Создано: {new Date(wish.created_at).toLocaleDateString("ru-RU", {
                    day: "numeric",
                    month: "short",
                    year: "numeric"
                  })}
                </div>
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

