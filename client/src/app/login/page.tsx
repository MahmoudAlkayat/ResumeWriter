"use client";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import React, { useState } from "react";
import { useToast } from "@/contexts/ToastProvider";

const Login: React.FC = () => {
  const { showError, showSuccess } = useToast();

  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [isForgotPassword, setIsForgotPassword] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      showError("Please enter your email and password");
      return;
    }
    // Login logic here
  };

  const handleForgotPassword = (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      showError("Please enter your email address");
      return;
    }
    // Forgot password logic here

    showSuccess("Password reset email sent");
    setTimeout(() => window.location.reload(), 3000);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center py-12 px-4 bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-lg shadow-xl">
        {/* Logo Section */}
        <div className="relative flex items-center justify-center">
          {isForgotPassword && (
            <i
              className="fas fa-arrow-left cursor-pointer text-gray-600 absolute left-0"
              onClick={() => setIsForgotPassword(false)}
            />
          )}
          <div className="text-center">
            <Link href="/">
              <h1 className="text-4xl font-bold text-gray-800 mb-2">
                EliteResume
              </h1>
            </Link>
            <p className="text-gray-600">
              {isForgotPassword
                ? "Reset your password"
                : "Login to get started"}
            </p>
          </div>
        </div>

        {/* Form Section */}
        <form
          className="mt-8 space-y-6"
          onSubmit={isForgotPassword ? handleForgotPassword : handleLogin}
        >
          {/* Email Input */}
          <div className="relative">
            <label
              htmlFor="email"
              className="text-sm font-medium text-gray-700 block mb-2"
            >
              Email Address
            </label>
            <div className="relative">
              <i className="fas fa-envelope absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
              <input
                id="email"
                name="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="pl-10 pr-3 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="Enter your email"
              />
            </div>
          </div>

          {/* Password Input (only for login) */}
          {!isForgotPassword && (
            <div className="relative">
              <label
                htmlFor="password"
                className="text-sm font-medium text-gray-700 block mb-2"
              >
                Password
              </label>
              <div className="relative">
                <i className="fas fa-lock absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
                <input
                  id="password"
                  name="password"
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pl-10 pr-10 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                  placeholder="Enter your password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 cursor-pointer"
                >
                  <i
                    className={`fas ${
                      showPassword ? "fa-eye-slash" : "fa-eye"
                    }`}
                  ></i>
                </button>
              </div>
            </div>
          )}

          {/* Remember Me & Forgot Password */}
          {!isForgotPassword && (
            <div className="flex items-center justify-between">
              <div className="flex items-center">
                <input
                  id="remember-me"
                  name="remember-me"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer"
                />
                <label
                  htmlFor="remember-me"
                  className="ml-2 block text-sm text-gray-700 cursor-pointer"
                >
                  Remember me
                </label>
              </div>
              <div className="text-sm">
                <button
                  type="button"
                  onClick={() => setIsForgotPassword(true)}
                  className="font-medium text-blue-600 hover:text-blue-500 cursor-pointer"
                >
                  Forgot your password?
                </button>
              </div>
            </div>
          )}

          {/* Submit Button */}
          <Button
            type="submit"
            variant="blue"
            className="w-full text-md h-full"
          >
            {isForgotPassword ? "Send Reset Link" : "Sign in"}
          </Button>

          {/* Social Login */}
          {!isForgotPassword && (
            <div className="mt-1">
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-300"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white text-gray-500">
                    Or continue with
                  </span>
                </div>
              </div>
              <div className="mt-6 grid gap-3">
                <Button
                  type="button"
                  onClick={() => {
                    return;

                    // Google OAuth2.0 configuration
                    const clientId = "your-google-client-id";
                    const redirectUri = encodeURIComponent(
                      window.location.origin + "/auth/google/callback"
                    );
                    const scope = encodeURIComponent("email profile");
                    const responseType = "code";
                    const accessType = "offline";
                    const prompt = "consent";

                    // Construct Google OAuth URL
                    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}&scope=${scope}&access_type=${accessType}&prompt=${prompt}`;

                    // Redirect to Google sign-in page
                    window.location.href = googleAuthUrl;
                  }}
                  className="text-md bg-white py-2 px-4 border-2 border-gray-300 rounded-md hover:bg-gray-100  cursor-pointer whitespace-nowrap text-gray-600"
                >
                  <i className="fab fa-google mr-1"></i>
                  Google
                </Button>
                {/* <button
                type="button"
                className="!rounded-button w-full py-2 px-4 border border-gray-300 bg-white text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 cursor-pointer whitespace-nowrap"
              >
                <i className="fab fa-linkedin mr-2"></i>
                LinkedIn
              </button> */}
              </div>
            </div>
          )}
        </form>

        {/* Sign Up Link */}
        {!isForgotPassword && (
          <div className="text-center mt-4">
            <p className="text-sm text-gray-600">
              Don't have an account?{" "}
              <a
                href="/register"
                className="font-medium text-blue-600 hover:text-blue-500"
              >
                Sign up
              </a>
            </p>
          </div>
        )}

        {/* Footer */}
        <div className="text-center mt-4 text-xs text-gray-500">
          <p>
            By continuing, you agree to our{" "}
            <span className="text-blue-600 hover:text-blue-500">
              Terms of Service
            </span>{" "}
            and{" "}
            <span className="text-blue-600 hover:text-blue-500">
              Privacy Policy
            </span>
          </p>
        </div>
      </div>
    </div>
  );
};
export default Login;
