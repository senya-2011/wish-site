import "./App.css";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthPage } from "./components/AuthPage";
import { WishDashboard } from "./components/WishDashboard";
import { MyWishesPage } from "./components/wishes/MyWishesPage";
import { SearchWishesPage } from "./components/wishes/SearchWishesPage";
import { WishDetailPage } from "./components/wishes/WishDetailPage";
import { UserWishesPage } from "./components/users/UserWishesPage";
import { useAuth } from "./context/useAuth";

function App() {
  const { token, user, logout } = useAuth();

  if (!token) {
    return <AuthPage />;
  }

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<WishDashboard user={user} onLogout={logout} />}>
          <Route index element={<Navigate to="/my-wishes" replace />} />
          <Route path="my-wishes" element={<MyWishesPage key={user?.user_id ?? "guest"} />} />
          <Route path="search" element={<SearchWishesPage />} />
          <Route path="wishes/:wishId" element={<WishDetailPage />} />
          <Route path="users/:userId/wishes" element={<UserWishesPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
