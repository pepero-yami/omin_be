CREATE INDEX idx_p_store_coordinates
    ON p_store
        USING GIST (coordinates);