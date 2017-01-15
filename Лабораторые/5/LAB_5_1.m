function [] = LAB_5_1() 
    clc;
    close all;
    clear all;
    deltas = [0.9 0.75 0.5 0.4 0.3 0.2 0.1];
    
    for delta = deltas
        [A, B] = field_sample(delta);
        R = originals(delta);
    
        grid = get_range(delta);
        [xx, yy] = meshgrid(grid, grid);
        
        [X, residuals, iterations] = gauss_seidel(A, B);
        residuals = residuals(1 : iterations);
        
        figure;
        plot(1 : iterations, residuals);
        title(strcat('Dependence of maximum residual of iterations count for delta=', num2str(delta)), 'FontSize', 12);
        xlabel('Iterations');
        ylabel('Maximum residual');
        fprintf('Maximum residual for discretization with delta=%4.2f after %d iterations: %E\n', delta, iterations, residuals(iterations));
        
        figure;
        zz = reshape(X, size(grid, 2), size(grid, 2));
        surf(xx, yy, zz);
        title(strcat('Seidel solution for delta=', num2str(delta)), 'FontSize', 12);
        axis([-1 1 -1 1 -0.3 0]);
        colorbar;
        
        figure;
        relative_error = abs((X - R) ./ R);
        zz = reshape(relative_error, size(grid, 2), size(grid, 2));
        surf(xx, yy, zz);
        title(strcat('Relative error for delta=', num2str(delta)), 'FontSize', 12);
        colorbar;
        
        relative_error(isnan(relative_error)) = 0; 
        fprintf('Average relative error for discretization with delta=%4.2f : %E\n', delta, mean(relative_error));
    end
    
    figure;
    zz = reshape(R, size(grid, 2), size(grid, 2));
    surf(xx, yy, zz);
    title('Analythical solution', 'FontSize', 12);
    axis([-1 1 -1 1 -0.3 0]);
    colorbar;
end

function [A, B] = field_sample(delta)
    N = ceil(1 / delta);
    n = 2 * N + 1;
    A = sparse(n^2, n^2);
    B = sparse(n^2, 1);
    
    for i = 1 : n^2
        [k, j] = restore(i, N);
        if (k^2 + j^2 < 1 / delta^2)
            B(i) = delta^2;
        end
    end
    
    for i = 1 : n^2
        [k, j] = restore(i, N);
        for z = 1 : n^2
            [k1, j1] = restore(z, N);
            
            if (k == k1 && j == j1)
                A(i, z) = -4;
            end
            
            if (abs(k - k1) == 1 && j == j1 && k^2 + j^2 < 1 / delta^2)
                A(i, z) = 1;
            end
            
            if (abs(j - j1) == 1 && k == k1 && k^2 + j^2 < 1 / delta^2)
                A(i, z) = 1;
            end
        end
    end
end

function [k, j] = restore(n, N)
    k = floor((n - 1) / (2 * N + 1)) - N;
    j = mod(n - 1, 2 * N + 1) - N;
end

function [x] = get_range(delta)
    N = ceil(1 / delta);
    n = 2 * N + 1;
    range = (n - 1) / 2 * delta;
    x = -range : delta : range;
end

function [R] = originals(delta)
    N = ceil(1 / delta);
    n = 2 * N + 1;
    R = sparse(n^2, 1);
    
    for i = 1 : n^2
        [k, j] = restore(i, N);
        x = k * delta;
        y = j * delta;
        if (x^2 + y^2 <= 1)
            R(i) = (x^2 + y^2 - 1) / 4;
        end
    end
end

function [X, residuals, iterations] = gauss_seidel(A, B)
    n = length(A);
    X = zeros(n, 1);
    max_iterations = 2000;
    residuals = zeros(max_iterations, 1);
    
    iterations = 0;
    while (iterations < max_iterations)
        iterations = iterations + 1;
        for i = 1 : n
            X(i) = (1 / A(i, i)) * (B(i) - A(i, 1 : n) * X + A(i, i) * X(i));
        end
        
        error = abs(A * X - B); 
        residuals(iterations) = max(error);
        if (error < 5E-16)
            break;
        end
    end
end