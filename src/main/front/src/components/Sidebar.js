import React from 'react';

const Sidebar = () => {
    return (
        <aside className="bg-white shadow-md w-80 fixed top-0 right-0 bottom-0 flex flex-col hidden lg:flex">
            <div className="flex-grow overflow-y-auto pt-24">
                <div className="px-8 py-6">
                    <div className="mb-10">
                        <h2 className="text-2xl font-bold mb-4">About</h2>
                        <p className="text-gray-700">Welcome to my tech blog. Here, I share my thoughts and experiences in the world of software development.</p>
                    </div>
                </div>
            </div>
            <div className="px-8 py-6 text-sm text-gray-600 border-t">
                <p className="font-semibold mb-2">양인영</p>
                <div className="space-y-2">
                    <a className="flex items-center text-gray-600 hover:text-blue-600 transition-colors duration-300">
                        <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg">
                            <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z"></path>
                            <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z"></path>
                        </svg>
                        inyoungyang01@gmail.com
                    </a>
                    <a
                        href="https://github.com/inyoung4121"
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex items-center text-gray-600 hover:text-blue-600 transition-colors duration-300"
                    >
                        <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                            <path d="M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12"></path>
                        </svg>
                        GitHub
                    </a>
                </div>
            </div>
        </aside>
    );
};

export default Sidebar;