"use client";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import React, { useEffect, useRef, useState, Suspense } from "react";
import { useToast } from "@/contexts/ToastProvider";
import { useAuth } from "@/hooks/auth";
import { API_URL } from "@/lib/config";
import { useRouter } from "next/navigation";
import { Background } from "@/components/ui/background";
import { useSearchParams } from "next/navigation";
import LoadingScreen from "@/components/LoadingScreen";
import { useAuthRedirect } from "@/hooks/auth";

// Separate component for the login form content
const LoginForm: React.FC = () => {
  const { showError, showSuccess } = useToast();
  const { login } = useAuth();
  const router = useRouter();
  const searchParams = useSearchParams();
  const verified = searchParams.get("verified");
  const verificationError = searchParams.get("verification");

  const [showPassword, setShowPassword] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [rememberMe, setRememberMe] = useState(false);
  const [isForgotPassword, setIsForgotPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const isMounted = useRef(false);

  useEffect(() => {
    if (verified === "true" && !isMounted.current) {
      showSuccess("Your account has been verified");
      isMounted.current = true;
    }
  }, [verified]);

  useEffect(() => {
    if (verificationError) {
      if (verificationError === "invalid") {
        showError("Invalid verification token");
      } else if (verificationError === "expired") {
        showError("Verification token has expired");
      }
    }
  }, [verificationError]);

  //Redirect logic and avoiding initial page render
  const { isAuthLoading } = useAuthRedirect({
    redirectTo: '/home',
    protectedRoute: false
  });

  if (isAuthLoading) {
    return <LoadingScreen />;
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email || !password) {
      showError("Please enter your email and password");
      return;
    }

    try {
      setIsLoading(true);

      const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ email, password }),
        credentials: "include" //Backend returns a session cookie
      });

      const profileResponse = await fetch(`${API_URL}/api/profile`, {
        method: "GET",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include"
      });

      if (!response.ok) {
        throw new Error(await response.text() || "Failed to login")
      }

      const user = await response.json();
      const profile = await profileResponse.json();
      showSuccess(`Welcome ${user.firstName}`);
      login(user, profile.themePreference)
      await new Promise(resolve => setTimeout(resolve, 1000))
      router.replace("/home")

    } catch (error) {
      if (error instanceof Error) {
        showError(error.message || "An unknown error occurred");
      } else {
        showError("An unknown error occurred");
      }
    } finally {
      setIsLoading(false)
    }
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!email) {
      showError("Please enter your email address");
      return;
    }
  
    try {
      setIsLoading(true);
      // Call the forgot-password endpoint with the email as a query parameter.
      const response = await fetch(`${API_URL}/auth/forgot-password?email=${encodeURIComponent(email)}`, {
        method: "POST",
        credentials: "include",
      });
  
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to send reset link");
      }
  
      showSuccess("Password reset email sent");
      setEmail(""); // Clear the email field
      setTimeout(() => {
        setIsForgotPassword(false); // Return to login view
      }, 2000);
    } catch (error) {
      if (error instanceof Error) {
        showError(error.message);
      } else {
        showError("An unexpected error occurred");
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
              {isForgotPassword ? "Send a password reset link to your email" : "Login to get started"}
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
                  <i className={`fas ${showPassword ? "fa-eye-slash" : "fa-eye"}`}></i>
                </button>
              </div>
            </div>
          )}

          {/* Remember Me & Forgot Password (only for login) */}
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
            disabled={isLoading}
          >
            {isLoading ? (
              <span className="flex items-center justify-center">
                <i className="fas fa-spinner fa-spin mr-2"></i>
                {isForgotPassword ? "Sending..." : "Signing in..."}
              </span>
            ) : (
              isForgotPassword ? "Send Reset Link" : "Sign in"
            )}
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
              <div className="flex mt-6 gap-3 justify-center">
              <div className="w-1/2">
                <Button
                  type="button"
                  onClick={() => {
                    // Step 1: Set your Google OAuth parameters
                    const clientId = "1090175341224-fmv0448rh2t2km73hgs3mbmdqtr6l3ba.apps.googleusercontent.com";
                    const redirectUri = encodeURIComponent("http://localhost:8080/oauth/google/callback");
                    const scope = encodeURIComponent("profile email");
                    const responseType = "code";
                    const accessType = "offline";
                    const prompt = "consent";

                    // Step 2: Construct the Google authorization URL
                    const googleAuthUrl = `https://accounts.google.com/o/oauth2/v2/auth?client_id=${clientId}&redirect_uri=${redirectUri}&response_type=${responseType}&scope=${scope}&access_type=${accessType}&prompt=${prompt}`;

                    // Step 3: Redirect the user to Google
                    window.location.replace(googleAuthUrl)
                  }}
                  className="w-full text-md bg-white py-2 px-4 border-2 border-gray-300 rounded-md hover:bg-gray-100 cursor-pointer whitespace-nowrap text-gray-600">
                  <i className="fab fa-google mr-1"></i>
                  Google
                </Button>
              </div>
              <div className="w-1/2">
                <Button
                  type="button"
                  onClick={() => {
                    const clientId = "78n9xakju4t060";
                    const redirectUri = encodeURIComponent("http://localhost:8080/oauth/linkedin/callback");
                    const scope = encodeURIComponent("openid email profile");
                    const responseType = "code";

                    const linkedinAuthUrl = `https://www.linkedin.com/oauth/v2/authorization?response_type=${responseType}&client_id=${clientId}&redirect_uri=${redirectUri}&scope=${scope}`;

                    window.location.replace(linkedinAuthUrl);
                  }}
                  className="w-full text-md bg-white py-2 px-4 border-2 border-gray-300 rounded-md hover:bg-gray-100 cursor-pointer whitespace-nowrap text-gray-600">
                  <i className="fab fa-linkedin mr-1"></i>
                  LinkedIn
                </Button>
              </div>
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
    </Background>
  );
};

const Login: React.FC = () => {
  const { isAuthLoading } = useAuthRedirect({
    redirectTo: '/home',
    protectedRoute: false
  });

  if (isAuthLoading) {
    return <LoadingScreen />;
  }

  return (
    <Suspense fallback={<LoadingScreen />}>
      <LoginForm />
    </Suspense>
  );
};

export default Login;
