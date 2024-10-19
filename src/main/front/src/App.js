import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import MainPage from './components/MainPage';
import LoginPage from './components/LoginPage';
import PostDetail from './components/PostDetail';
import CreatePost from './components/CreatePost';


const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/api/posts/:postId" element={<PostDetail />} />
                <Route path="/login" element={<LoginPage />} />
                <Route path="/create/post" element={<CreatePost />} />
            </Routes>
        </Router>
    );
};

export default App;