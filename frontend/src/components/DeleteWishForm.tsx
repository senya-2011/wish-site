import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi } from "../lib/api-client";

export const DeleteWishForm = () => {
  const [wishId, setWishId] = useState("");

  const mutation = useMutation({
    mutationFn: async (id: number) => {
      await wishesApi.deleteWishById(id);
      return id;
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
    setWishId("");
  };

  return (
    <div className="card">
      <h3>Удалить желание</h3>
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
          className="button danger"
          disabled={mutation.isPending}
        >
          {mutation.isPending ? "Удаляем..." : "Удалить"}
        </button>
      </form>
      {mutation.isError && (
        <p className="error">Ошибка при удалении желания</p>
      )}
      {mutation.isSuccess && (
        <p className="notice">Желание удалено</p>
      )}
    </div>
  );
};

