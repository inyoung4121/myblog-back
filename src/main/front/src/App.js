import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import MainPage from './components/MainPage';
import PostDetail from './components/PostDetail';

const App = () => {
    return (
        <Router>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/api/posts/:postId" element={<PostDetail />} />
            </Routes>
        </Router>
    );
};

export default App;