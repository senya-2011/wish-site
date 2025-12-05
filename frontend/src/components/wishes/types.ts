import type { WishResponse } from "../../api";

export type WishFormState = {
  title: string;
  description: string;
  photoUrl: string;
  price: string;
  photoFile?: File | null;
};

export type WishListHandlers = {
  onSelect: (wish: WishResponse) => void;
  onEdit: (wish: WishResponse) => void;
  onDelete: (wish: WishResponse) => void;
};

