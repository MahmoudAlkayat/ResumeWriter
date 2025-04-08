import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import { ToastProvider } from "@/contexts/ToastProvider";
import { AuthProvider } from "@/contexts/AuthProvider";
import { ResumeProcessingProvider } from "@/contexts/ResumeProcessingProvider";
import { Toaster } from "sonner";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "EliteResume",
  description: "AI-powered resume and cover letter generation",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" suppressHydrationWarning>
      <head>
        <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" />
        <link rel="icon" type="image/svg+xml" href="/icon.svg" />
        <script
            dangerouslySetInnerHTML={{
              __html: `
                (function () {
                try {
                  const theme = localStorage.getItem('theme');
                  
                  if (theme === 'dark') {
                    document.documentElement.classList.add('dark');
                  } else {
                    document.documentElement.classList.remove('dark');
                    localStorage.setItem('theme', 'light');
                  }
                } catch (_) {}
              })();
              `,
            }}
          />
      </head>
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased`}
      >
        <ToastProvider>
        <AuthProvider>
            <Toaster position="top-center" duration={3000} />
            {children}
        </AuthProvider>
        </ToastProvider>
      </body>
    </html>
  );
}
