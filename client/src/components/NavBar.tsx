import { useAuth } from "@/hooks/auth";
import { Button } from "@/components/ui/button";
import {User, LogOut, Loader2, CircleEllipsis } from "lucide-react";
import Link from "next/link";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuLabel,
    DropdownMenuSeparator,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar";
import { Logo } from "@/components/Logo";
import { useResumeProcessing } from "@/contexts/ResumeProcessingProvider";
import Image from "next/image";
import { StatusDialog } from "@/components/StatusDialog";
import { useState } from "react";

export default function NavBar() {
    const { user, logout } = useAuth();
    const { activeResumeId, activeFreeformId } = useResumeProcessing();
    const isProcessing = activeResumeId !== null || activeFreeformId !== null;
    const [statusDialogOpen, setStatusDialogOpen] = useState(false);

    return (
        <nav className="sticky top-0 z-50 w-full bg-background dark:bg-sidebar shadow-md">
            <div className="flex h-14 items-center px-4">
                <div className="flex items-center gap-2">
                    <Image src="/logo.png" alt="Logo" width={48} height={48} />
                    <Link href="/home">
                        <Logo />
                    </Link>
                    {isProcessing && (
                        <div className="flex items-center gap-2 text-md text-green-600 ml-8">
                            <Loader2 className="h-4 w-4 animate-spin" />
                            <span>Processing {activeResumeId ? "Resume Upload" : "Career Entry"}</span>
                        </div>
                    )}
                </div>
                <div className="flex-1" />
                <div className="flex items-center gap-2">
                    <StatusDialog open={statusDialogOpen} onOpenChange={setStatusDialogOpen} />
                    <DropdownMenu modal={false}>
                        <DropdownMenuTrigger asChild>
                            <Button 
                                variant="ghost" 
                                size="default"
                                className="!h-auto focus:outline-none flex items-center gap-4 px-4 py-1 hover:bg-accent"
                            >
                                <Avatar className="size-8">
                                    <AvatarImage src={`https://api.dicebear.com/9.x/initials/svg?seed=${user?.firstName}${user?.lastName}&backgroundType=gradientLinear`} />
                                    <AvatarFallback>{user?.firstName[0]}</AvatarFallback>
                                </Avatar>
                                <span className="text-lg font-medium">{user?.firstName}</span>
                            </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="w-36 mt-4">
                            <DropdownMenuLabel>
                                <div className="flex flex-col items-center">
                                    {user?.firstName} {user?.lastName}
                                    <span className="text-muted-foreground">
                                        {user?.email}
                                    </span>
                                </div>
                            </DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem asChild>
                                <Link href="/profile" className="cursor-pointer">
                                    <User className="mr-2 h-4 w-4" />
                                    <span>Profile</span>
                                </Link>
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem onClick={() => setStatusDialogOpen(true)} className="cursor-pointer">
                                <CircleEllipsis className="mr-2 h-4 w-4" />
                                <span>Status</span>
                            </DropdownMenuItem>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem 
                                onClick={logout}
                                className="text-red-600 focus:text-destructive cursor-pointer"
                            >
                                <LogOut className="mr-2 h-4 w-4" />
                                <span>Log out</span>
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                    </DropdownMenu>
                </div>
            </div>
        </nav>
    );
}