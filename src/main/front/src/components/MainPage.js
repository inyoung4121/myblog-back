import React, { useState, useEffect, useRef, useCallback } from 'react';
import BlogPost from './BlogPost';
import Sidebar from './Sidebar';
import Header from './Header';

const MainPage = () => {
    const [posts, setPosts] = useState([]);
    const [page, setPage] = useState(0);
    const [loading, setLoading] = useState(false);
    const [hasMore, setHasMore] = useState(true);
    const [error, setError] = useState(null);
    const lastPostElementRef = useRef(null);
    const initialFetchRef = useRef(false);

    const fetchPosts = useCallback(async () => {
        if (loading || !hasMore) return;
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`/api/posts?page=${page}&size=10`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const data = await response.json();
            setPosts(prevPosts => [...prevPosts, ...data.content]);
            setHasMore(!data.last);
            setPage(prevPage => prevPage + 1);
        } catch (error) {
            setError('Failed to fetch posts. Please try again later.');
        } finally {
            setLoading(false);
        }
    }, [page, loading, hasMore]);

    useEffect(() => {
        if (!initialFetchRef.current) {
            fetchPosts();
            initialFetchRef.current = true;
        }
    }, [fetchPosts]);

    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => {
                if (entries[0].isIntersecting && hasMore && !loading) {
                    fetchPosts();
                }
            },
            { threshold: 1 }
        );

        if (lastPostElementRef.current) {
            observer.observe(lastPostElementRef.current);
        }

        return () => {
            if (lastPostElementRef.current) {
                observer.unobserve(lastPostElementRef.current);
            }
        };
    }, [fetchPosts, hasMore, loading]);

    return (
        <div className="bg-gray-0 min-h-screen flex flex-col">
            <Header />
            <div className="flex-grow flex pt-16">
                <main className="flex-grow overflow-y-auto lg:pr-80">
                    <div className="container mx-auto px-4 py-8">
                        <div className="grid grid-cols-1 sm:grid-cols-2 gap-8 mx-8">
                            {posts.map((post, index) => (
                                <BlogPost
                                    key={post.id}
                                    post={post}
                                    ref={index === posts.length - 1 ? lastPostElementRef : null}
                                />
                            ))}
                        </div>
                        {loading && <p className="text-center mt-4 col-span-full">Loading...</p>}
                        {error && <p className="text-red-500 text-center mt-4 col-span-full">{error}</p>}
                    </div>
                </main>
                <Sidebar/>
            </div>
        </div>
    );
};

export default MainPage;