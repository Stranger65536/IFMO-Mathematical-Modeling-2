function [] = LAB_6() 
    clc;
    close all;
    clear all;

    [x, y, tri] = initial_triangulation;
    [x, y, tri] = refine_triangulation(x, y, tri, 0);
    [x, y, tri] = refine_triangulation(x, y, tri, 1);
    
    z = -(x.^2 + y.^2);
    trimesh(tri,x,y,z);
    
    n = length(tri);
    R = originals(x, y);
    
    M = sparse(n, n);
    
    for i = 1 : n
        for j = 1 : n
            [a, b, c, found] = find_triangle(i, j, tri);
            
            if (found)
                ax = x(a); ay = y(a);
                bx = x(b); by = y(b);
                cx = x(c); cy = y(c);
                z = 10;
            end
        end
    end
    
    for i = 1 : n
        edges = tri(i, :);
        a = edges(1);
        b = edges(2);
        c = edges(3);
        ax = x(a); ay = y(a);
        bx = x(b); by = y(b);
        cx = x(c); cy = y(c);
    end
%         scatter(x,y);
%     axis([-1 1 -1 1])
    
%     for delta = deltas
%         [A, B] = field_sample(delta);
%         R = originals(delta);
%     
%         grid = get_range(delta);
%         [xx, yy] = meshgrid(grid, grid);
%         
%         [X, residuals, iterations] = gauss_seidel(A, B);
%         residuals = residuals(1 : iterations);
%         
%         figure;
%         plot(1 : iterations, residuals);
%         title(strcat('Dependence of maximum residual of iterations count for delta=', num2str(delta)), 'FontSize', 12);
%         xlabel('Iterations');
%         ylabel('Maximum residual');
%         fprintf('Maximum residual for discretization with delta=%4.2f after %d iterations: %E\n', delta, iterations, residuals(iterations));
%         
%         figure;
%         zz = reshape(X, size(grid, 2), size(grid, 2));
%         surf(xx, yy, zz);
%         title(strcat('Seidel solution for delta=', num2str(delta)), 'FontSize', 12);
%         axis([-1 1 -1 1 -0.3 0]);
%         colorbar;
%         
%         figure;
%         relative_error = abs((X - R) ./ R);
%         zz = reshape(relative_error, size(grid, 2), size(grid, 2));
%         surf(xx, yy, zz);
%         title(strcat('Relative error for delta=', num2str(delta)), 'FontSize', 12);
%         colorbar;
%         
%         relative_error(isnan(relative_error)) = 0; 
%         fprintf('Average relative error for discretization with delta=%4.2f : %E\n', delta, mean(relative_error));
%     end
%     
%     figure;
%     zz = reshape(R, size(grid, 2), size(grid, 2));
%     surf(xx, yy, zz);
%     title('Analythical solution', 'FontSize', 12);
%     axis([-1 1 -1 1 -0.3 0]);
%     colorbar;
end

function [a, b, c, found] = find_triangle(i, j, tri)
    found = 0;

    for k = 1 : length(tri)
        row = tri(k, :);
        if (i == j)
            if (~any(~ismember(i, row)))
                a = i;
                b = i;
                c = row(row ~= i);
                c = c(1);
                found = 1;
                break;
            end
        else
            if (~any(~ismember([i j], row)))
                a = i;
                b = j;
                c = row(row ~= i & row ~= j);
                c = c(1);
                found = 1;
                break;
            end
        end
    end
    
    if (found == 0)
        a = 0;
        b = 0;
        c = 0;
    end
end

function [R] = originals(x, y)
    n = length(x);
    R = zeros(n, 1);
    
    for i = 1 : n
        R(i) = (x(i)^2 + y(i)^2 - 1) / 4;
    end
end

function [x, y, tri] = initial_triangulation()
    alpha = sqrt(3)/2;
    x = zeros(10, 2);
    x(1,:) = [0 0];
    x(2,:) = [0 1];
    x(3,:) = [0 -1];
    x(4,:) = [-alpha -0.5];
    x(5,:) = [alpha 0.5];
    x(6,:) = [-alpha 0.5];
    x(7,:) = [alpha -0.5];
    x(8,:) = [-2*alpha 0];
    x(9,:) = [2*alpha 0];
    x(10,:) = [-alpha 1.5];
    x(11,:) = [-alpha -1.5];
    x(12,:) = [alpha 1.5];
    x(13,:) = [alpha -1.5];
    y = x(:, 2);
    x = x(:, 1);
    tri = delaunay(x, y);
end

function [x, y, tri] = refine_triangulation(x, y, tri, shrink)
    n = length(x);
    triangles_count = length(tri); 
    new_n = (n - 1) * 3 + 1;
    
    points = zeros(new_n, 2);
    count = 0;
    
    for i = 1 : triangles_count
        edges = tri(i, :);
        a = edges(1);
        b = edges(2);
        c = edges(3);
        ax = x(a); ay = y(a);
        bx = x(b); by = y(b);
        cx = x(c); cy = y(c);
        
        part = points(1:count, :);
        if (any(~ismember([ax ay], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [ax, ay]; 
        end
        if (any(~ismember([bx by], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [bx, by]; 
        end
        if (any(~ismember([cx cy], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [cx, cy]; 
        end
        
        abx = (ax + bx) / 2; aby = (ay + by) / 2;
        bcx = (bx + cx) / 2; bcy = (by + cy) / 2;
        cax = (cx + ax) / 2; cay = (cy + ay) / 2;
        
        part = points(1:count, :);
        if (any(~ismember([abx aby], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [abx, aby]; 
        end
        if (any(~ismember([bcx bcy], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [bcx, bcy]; 
        end
        if (any(~ismember([cax cay], part, 'rows') ~= 0))
            count = count + 1;
            points(count, :) = [cax, cay]; 
        end
    end

    if (shrink)
        points = points(points(:,1).^2 + points(:,2).^2 < 1, :);
    end
    
    x = points(:, 1);
    y = points(:, 2);
    tri = delaunay(x, y);
end

function [A, B] = sample_field(x, y, tri)
    n = length(x);
    A = sparse(n^2, n^2);
    B = sparse(n^2, 1);
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