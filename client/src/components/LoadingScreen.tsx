import { Background } from "./ui/background";

const LoadingScreen = ({ message }: { message?: string }) => {
    return (
        <Background className="h-screen flex flex-col items-center justify-center">
            <i className="fas fa-spinner fa-spin text-5xl mb-4"></i>
            <p className="text-lg">{message || "Loading..."}</p>
        </Background>
    );
};

export default LoadingScreen;
