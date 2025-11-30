import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi } from "../lib/api-client";

export const GetWishById = () => {
  const [wishId, setWishId] = useState("");

  const mutation = useMutation({
    mutationFn: async (id: number) => {
      const response = await wishesApi.getWishById(id);
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
    mutation.mutate(id);
  };

  return (
    <div className="card">
      <h3>Получить желание по ID</h3>
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
        <button
          type="submit"
          className="button"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Загружаем..." : "Загрузить"}
        </button>
      </form>

      {mutation.isError && (
        <p className="error">Ошибка при загрузке желания</p>
      )}

      {mutation.isSuccess && (
        <pre className="response-box">
          {JSON.stringify(mutation.data, null, 2)}
        </pre>
      )}
    </div>
  );
};

