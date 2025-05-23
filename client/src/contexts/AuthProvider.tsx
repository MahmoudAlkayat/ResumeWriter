"use client";
import { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useToast } from "@/contexts/ToastProvider";
import { API_URL } from "@/lib/config";
import { useTheme } from "next-themes";
export interface User {
  id: number,
  firstName: string,
  lastName: string,
  email: string
  profilePictureUrl?: string;
}

interface AuthContextType {
  isAuthenticated: boolean | null;
  isLogout: boolean;
  login: (user: User, themePreference: string) => void;
  logout: () => void;
  user: User | null;
}

export const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const { showError, showSuccess } = useToast();
  const [isAuthenticated, setIsAuthenticated] = useState<boolean | null>(null);
  const [isLogout, setIsLogout] = useState(false);
  const router = useRouter();
  const [user, setUser] = useState<User | null>(null);
  const { setTheme } = useTheme();

  const login = (user: User, themePreference: string) => {
    setIsAuthenticated(true);
    setUser(user);
    setIsLogout(false);
    setTheme(themePreference);
  };

  const logout = async () => {
    try {
      const response = await fetch(`${API_URL}/auth/logout`, {
        method: "POST",
        credentials: "include"
      });
      if (!response.ok) {
        throw new Error("Failed to logout");
      } else {
        setIsLogout(true);
        setIsAuthenticated(false);
        setUser(null);
        setTheme("light");
        if (!isLogout) {
          showSuccess("Successfully logged out");
        }
        router.push("/"); //Go to landing page on logout
      }
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      } else {
        showError("An unknown error occurred");
      }
    }
  };

  const checkAuth = async () => {
    try {
      const response = await fetch(`${API_URL}/auth/me`, {
        method: "GET",
        credentials: "include"
      });
      if (response.ok) {
        setIsAuthenticated(true);
        setUser(await response.json());
        setIsLogout(false);
      } else {
        setIsAuthenticated(false);
        setUser(null);
      }
    } catch (error) {
      showError("Failed to check authentication");
      setIsAuthenticated(false);
      setUser(null);
    }
  };

  useEffect(() => {
    checkAuth();
  }, [])

  return (
    <AuthContext.Provider value={{ isAuthenticated, isLogout, login, logout, user }}>
      {children}
    </AuthContext.Provider>
  );
};
