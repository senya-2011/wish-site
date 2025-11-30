import type { WishResponse } from "../../api";

export type WishFormState = {
  title: string;
  description: string;
  photoUrl: string;
  price: string;
};

export type WishListHandlers = {
  onSelect: (wish: WishResponse) => void;
  onEdit: (wish: WishResponse) => void;
  onDelete: (wish: WishResponse) => void;
};

