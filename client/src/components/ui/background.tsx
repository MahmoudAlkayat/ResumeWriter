"use client";

import { cn } from "@/lib/utils";

interface BackgroundProps {
  children: React.ReactNode;
  className?: string;
}

export function Background({ children, className }: BackgroundProps) {
  return (
    <div className={cn(
      "bg-gradient-to-br from-blue-200 via-white to-gray-100 overflow-hidden",
      className
    )}>
      {children}
    </div>
  );
}
