"use client"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import type React from "react"
import { useState } from "react"

const Register: React.FC = () => {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [firstName, setFirstName] = useState("")
  const [lastName, setLastName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [confirmPassword, setConfirmPassword] = useState("")
  const [agreeTerms, setAgreeTerms] = useState(false)
  const [formError, setFormError] = useState("")

  const handleRegister = (e: React.FormEvent) => {
    e.preventDefault()
    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      setFormError("Please fill in all fields")
      return
    }
    if (password !== confirmPassword) {
      setFormError("Passwords do not match")
      return
    }
    if (!agreeTerms) {
      setFormError("You must agree to the Terms of Service and Privacy Policy")
      return
    }
    setFormError("")

    // Registration logic
  }

  return (
    <div className="min-h-screen w-full flex items-center justify-center py-12 px-4 bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden">
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
          {formError && <div className="text-red-500 text-sm text-center">{formError}</div>}
          {/* Full Name Input */}
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
          {/* Email Input */}
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
          {/* Password Input */}
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
          {/* Confirm Password Input */}
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
          <Button type="submit" variant="blue" className="w-full text-md h-full">
            Create Account
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
            <div className="mt-6 grid gap-3">
              <Button
                type="button"
                onClick={() => {
                  return
                  // Google OAuth2.0 configuration would go here
                }}
                className="text-md bg-white py-2 px-4 border-2 border-gray-300 rounded-md hover:bg-gray-100 cursor-pointer whitespace-nowrap text-gray-600"
              >
                <i className="fab fa-google mr-1"></i>
                Google
              </Button>
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
    </div>
  )
}

export default Register

