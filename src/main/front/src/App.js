import React from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import {TagProvider} from './contexts/TagContext';
import MainPage from './components/MainPage';
import LoginPage from './components/LoginPage';
import PostDetail from './components/PostDetail';
import CreatePost from './components/CreatePost';
import './config/axiosConfig';
import PrivateRoute from "./route/PrivateRoute";
import { Helmet } from 'react-helmet';


const App = () => {
    return (
        <TagProvider>
            <Helmet>
                <title>Blog Eureka</title>
            </Helmet>
            <Router>
                <Routes>
                    <Route path="/" element={<MainPage/>}/>
                    <Route path="/api/posts/:postId" element={<PostDetail/>}/>
                    <Route path="/login" element={<LoginPage/>}/>
                    <Route
                        path="/create/post"
                        element={
                            <PrivateRoute requiredRoles={['ADMIN', 'MANAGER']}>
                                <CreatePost/>
                            </PrivateRoute>
                        }
                    />
                </Routes>
            </Router>
        </TagProvider>
    );
};

export default App;