"use client";
import { Button } from "@/components/ui/button";
import React, { useEffect, useState, Suspense } from "react";
import { useToast } from "@/contexts/ToastProvider";
import { useRouter, useSearchParams } from "next/navigation";
import { Background } from "@/components/ui/background";
import { API_URL } from "@/lib/config";
import LoadingScreen from "@/components/LoadingScreen";

const ResetPasswordForm: React.FC = () => {
  const { showError, showSuccess } = useToast();
  const router = useRouter();
  const searchParams = useSearchParams();
  const token = searchParams.get("token");

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  
  useEffect(() => {
    if (!token) {
      showError("Invalid token");
      router.push("/login");
    }
  }, [token, router, showError]);

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!newPassword || !confirmPassword) {
      showError("Please enter a new password");
      return;
    }

    if (newPassword !== confirmPassword) {
      showError("Passwords do not match");
      return;
    }

    try {
      setIsLoading(true);

      const response = await fetch(`${API_URL}/auth/reset-password?token=${token}`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ newPassword }),
        credentials: "include"
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Password reset failed")
      }

      showSuccess("Password reset successful");
      setNewPassword("");
      setConfirmPassword("");
      await new Promise(resolve => setTimeout(resolve, 500))
      router.push("/login");
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message || "An unknown error occurred");
      } else {
        showError("An unknown error occurred");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Background className="min-h-screen flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-lg shadow-xl">
        {/* Logo Section */}
        <div className="relative flex items-center justify-center">
          <div className="text-center">
            <p className="text-black font-bold text-2xl">Reset Your Password</p>
          </div>
        </div>

        {/* Form Section */}
        <form className="mt-8 space-y-6" onSubmit={handleResetPassword}>
          {/* New Password Input */}
          <div className="relative">
            <label
              htmlFor="newPassword"
              className="text-sm font-medium text-gray-700 block mb-2"
            >
              New Password
            </label>
            <div className="relative">
              <i className="fas fa-lock absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
              <input
                id="newPassword"
                name="newPassword"
                type={showPassword ? "text" : "password"}
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="pl-10 pr-10 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="Enter new password"
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer"
              >
                <i className={`fas ${showPassword ? "fa-eye-slash" : "fa-eye"}`}></i>
              </button>
            </div>
          </div>

          {/* Confirm Password Input */}
          <div className="relative">
            <label
              htmlFor="confirmPassword"
              className="text-sm font-medium text-gray-700 block mb-2"
            >
              Confirm Password
            </label>
            <div className="relative">
              <i className="fas fa-lock absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
              <input
                id="confirmPassword"
                name="confirmPassword"
                type={showConfirmPassword ? "text" : "password"}
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                className="pl-10 pr-10 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="Confirm new password"
              />
              <button
                type="button"
                onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer"
              >
                <i className={`fas ${showConfirmPassword ? "fa-eye-slash" : "fa-eye"}`}></i>
              </button>
            </div>
          </div>

          {/* Submit Button */}
          <div>
            <Button
              type="submit"
              className="w-full"
              disabled={isLoading}
              variant={"blue"}
            >
              {isLoading ? (
                <span className="flex items-center justify-center">
                  <i className="fas fa-spinner fa-spin mr-2"></i>
                  Resetting...
                </span>
              ) : (
               "Reset Password" 
              )}
            </Button>
          </div>
        </form>
      </div>
    </Background>
  );
};

const ResetPassword: React.FC = () => {
  return (
    <Suspense fallback={<LoadingScreen />}>
      <ResetPasswordForm />
    </Suspense>
  );
};

export default ResetPassword;