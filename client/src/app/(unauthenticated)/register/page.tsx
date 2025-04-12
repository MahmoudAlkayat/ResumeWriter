"use client";
import { Button } from "@/components/ui/button";
import Link from "next/link";
import React, { useState } from "react";
import { API_URL } from "@/lib/config"
import { useToast } from "@/contexts/ToastProvider";
import { Background } from "@/components/ui/background";
import LoadingScreen from "@/components/LoadingScreen";
import { useAuthRedirect } from "@/hooks/auth";

const Register: React.FC = () => {
  const { showError, showInfo } = useToast();

  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [agreeTerms, setAgreeTerms] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  //Redirect logic and avoiding initial page render
  const { isAuthLoading } = useAuthRedirect({
    redirectTo: '/home',
    protectedRoute: false
  });

  if (isAuthLoading) {
    return <LoadingScreen />;
  }

  const handleRegister = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    console.log("Register form submitted");

    // Validate input locally
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      showError("Please fill in all fields");
      return;
    }

    // Email validation
    if (!/^\w+([\.-]?\w+)*@\w+([\.-]?\w+)*(\.\w{2,3})+$/.test(email)) {
      showError("Please provide a valid email address");
      return;
    }
    //Strong password check
    if (password.length < 8) {
      showError("Password must be at least 8 characters long.");
      return;
    }
    if (!/[A-Z]/.test(password)) {
      showError("Password must contain at least one uppercase letter.");
      return;
    }
    if (!/[0-9]/.test(password)) {
      showError("Password must contain at least one number.");
      return;
    }
    if (password !== confirmPassword) {
      showError("Passwords do not match");
      return;
    }
    if (!agreeTerms) {
      showError("You must agree to the Terms of Service and Privacy Policy");
      return;
    }

    try {
      setIsLoading(true);
      const response = await fetch(`${API_URL}/api/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          firstName,
          lastName,
          email,
          password,
        }),
      });
      console.log("Fetch response received:", response);

      if (!response.ok) {
        const errorText = await response.text();
        console.error("Registration error:", errorText);
        throw new Error(errorText || "Registration failed");
      }

      const successText = await response.text();
      console.log("Registration success:", successText);
      showInfo("Registration successful! Please check your email or spam to verify your account.");
      setFirstName("");
      setLastName("");
      setEmail("");
      setPassword("");
      setConfirmPassword("");
      
    } catch (err: any) {
      console.error("Error during registration:", err);
      showError(err.message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Background className="min-h-screen flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-lg shadow-xl">
        {/* Logo Section */}
        <div className="text-center">
          <Link href="/">
            <h1 className="text-4xl font-bold text-gray-800 mb-2">EliteResume</h1>
          </Link>
          <p className="text-gray-600">Create your account to get started</p>
        </div>
        {/* Form Section */}
        <form className="mt-8 space-y-6" onSubmit={handleRegister}>
          {/* First Name */}
          <div className="relative">
            <label htmlFor="firstName" className="text-sm font-medium text-gray-700 block mb-2">
              First Name
            </label>
            <div className="relative">
              <i className="fas fa-user absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
              <input
                id="firstName"
                name="firstName"
                type="text"
                value={firstName}
                onChange={(e) => setFirstName(e.target.value)}
                className="pl-10 pr-3 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="Enter your first name"
              />
            </div>
          </div>
          {/* Last Name */}
          <div className="relative">
            <label htmlFor="lastName" className="text-sm font-medium text-gray-700 block mb-2">
              Last Name
            </label>
            <div className="relative">
              <i className="fas fa-user absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400"></i>
              <input
                id="lastName"
                name="lastName"
                type="text"
                value={lastName}
                onChange={(e) => setLastName(e.target.value)}
                className="pl-10 pr-3 py-2 w-full border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition-all"
                placeholder="Enter your last name"
              />
            </div>
          </div>
          {/* Email */}
          <div className="relative">
            <label htmlFor="email" className="text-sm font-medium text-gray-700 block mb-2">
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
          {/* Password */}
          <div className="relative">
            <label htmlFor="password" className="text-sm font-medium text-gray-700 block mb-2">
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
                placeholder="Create a password"
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
          {/* Confirm Password */}
          <div className="relative">
            <label htmlFor="confirmPassword" className="text-sm font-medium text-gray-700 block mb-2">
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
                placeholder="Confirm your password"
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
          {/* Terms and Conditions */}
          <div className="flex items-center">
            <input
              id="agree-terms"
              name="agree-terms"
              type="checkbox"
              checked={agreeTerms}
              onChange={(e) => setAgreeTerms(e.target.checked)}
              className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer"
            />
            <label htmlFor="agree-terms" className="ml-2 block text-sm text-gray-700 cursor-pointer">
              I agree to the <span className="text-blue-600 hover:text-blue-500 cursor-pointer">Terms of Service</span>{" "}
              and <span className="text-blue-600 hover:text-blue-500 cursor-pointer">Privacy Policy</span>
            </label>
          </div>
          {/* Register Button */}
          <Button 
            type="submit" 
            variant="blue" 
            className="w-full text-md h-full"
            disabled={isLoading}
          >
            {isLoading ? (
              <span className="flex items-center justify-center">
                <i className="fas fa-spinner fa-spin mr-2"></i>
                Registering...
              </span>
            ) : (
              "Create Account"
            )}
          </Button>
          {/* Social Registration */}
          <div className="mt-1">
            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-300"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-2 bg-white text-gray-500">Or sign up with</span>
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
                    window.location.replace(googleAuthUrl);
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
        </form>
        {/* Sign In Link */}
        <div className="text-center mt-4">
          <p className="text-sm text-gray-600">
            Already have an account?{" "}
            <a href="/login" className="font-medium text-blue-600 hover:text-blue-500">
              Sign in
            </a>
          </p>
        </div>
        {/* Footer */}
        <div className="text-center mt-4 text-xs text-gray-500">
          <p>
            By continuing, you agree to our <span className="text-blue-600 hover:text-blue-500">Terms of Service</span>{" "}
            and <span className="text-blue-600 hover:text-blue-500">Privacy Policy</span>
          </p>
        </div>
      </div>
    </Background>
  );
};

export default Register;
