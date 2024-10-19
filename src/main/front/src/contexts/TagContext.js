import React, { createContext, useState, useContext, useCallback } from 'react';

// Context 생성
const TagContext = createContext();

// Provider 컴포넌트
export const TagProvider = ({ children }) => {
    const [selectedTags, setSelectedTags] = useState([]);

    // 태그 토글 함수
    const toggleTag = useCallback((tag) => {
        setSelectedTags(prev =>
            prev.includes(tag)
                ? prev.filter(t => t !== tag)
                : [...prev, tag]
        );
    }, []);

    // 모든 태그 초기화 함수
    const clearTags = useCallback(() => {
        setSelectedTags([]);
    }, []);

    // Context에 제공할 값들
    const value = {
        selectedTags,
        toggleTag,
        clearTags
    };

    return (
        <TagContext.Provider value={value}>
            {children}
        </TagContext.Provider>
    );
};

// 커스텀 훅
export const useTag = () => {
    const context = useContext(TagContext);
    if (context === undefined) {
        throw new Error('useTag must be used within a TagProvider');
    }
    return context;
};