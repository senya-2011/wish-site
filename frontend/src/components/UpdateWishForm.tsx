import { useMutation } from "@tanstack/react-query";
import { useState } from "react";
import { wishesApi, filesApi } from "../lib/api-client";
import type { WishUpdateRequest } from "../api";

export const UpdateWishForm = () => {
  const [wishId, setWishId] = useState("");
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [photoUrl, setPhotoUrl] = useState("");
  const [price, setPrice] = useState("");
  const [isUploading, setIsUploading] = useState(false);
  const [uploadError, setUploadError] = useState<string | null>(null);

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

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setUploadError("Можно загружать только изображения");
      return;
    }

    const maxSize = 10 * 1024 * 1024;
    if (file.size > maxSize) {
      setUploadError("Размер файла не должен превышать 10MB");
      return;
    }

    setUploadError(null);
    setIsUploading(true);

    try {
      const response = await filesApi.uploadFile(file);
      const fileUrl = response.data.file_url;
      if (fileUrl) {
        setPhotoUrl(fileUrl);
      }
    } catch (error) {
      console.error("Upload error:", error);
      setUploadError("Не удалось загрузить файл");
    } finally {
      setIsUploading(false);
    }
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
        <div>
          <label className="form-label">
            Загрузить новое фото:
            <input
              type="file"
              accept="image/*"
              onChange={handleFileUpload}
              disabled={isUploading}
              className="form-input"
            />
          </label>
          {isUploading && <p>Загрузка...</p>}
          {uploadError && <p className="error">{uploadError}</p>}
          {photoUrl && (
            <div>
              <img src={photoUrl} alt="Preview" style={{ maxWidth: "200px", marginTop: "10px" }} />
              <button type="button" onClick={() => setPhotoUrl("")} className="button">
                Удалить фото
              </button>
            </div>
          )}
        </div>
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

