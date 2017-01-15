function [] = lab6()
    clc;
    close all;
    clear all;
    deltas = [0.4 0.3 0.2 0.1 0.05 0.025 0.0125];
    
    time = cputime;
    
    for delta = deltas
        [p, t, b] = field_sample(delta);
        R = originals(p);
        [A, B] = finite_elements(p, t, b);
        U = conjgrad(A, B, zeros(size(p,1),1)); %A \ B;
        
        figure;
        trisurf(t, p(:,1), p(:,2), U);
        title(strcat('Finite elements solution for point number=', num2str(size(p, 1))), 'FontSize', 12);
        axis([-1 1 -1 1 -0.3 0]);
        colorbar;
        
        figure;
        relative_error = abs((U - R) ./ R);
        trisurf(t, p(:,1), p(:,2), relative_error);
        title(strcat('Relative error for point number=', num2str(size(p, 1))), 'FontSize', 12);
        colorbar;
        axis([-1 1 -1 1]);
        colorbar;
        
        relative_error(isnan(relative_error)) = 0; 
        fprintf('Average relative error for point number = %6d : %E\n', size(p, 1), mean(relative_error));
    end

    figure;
    trisurf(t, p(:,1), p(:,2), R);
    title('Analythical solution', 'FontSize', 12);
    axis([-1 1 -1 1 -0.3 0]);
    colorbar;
    
    fprintf('Computing time : %f sec.\n',  cputime - time);
end

function [p, t, b] = field_sample(delta)
    fd = @(p) sqrt(sum(p.^2, 2)) - 1;
    [p, t] = distmesh2d(fd, @huniform, delta, [-1,-1;1,1] ,[]);
    b = unique(boundedges(p, t));
end

function [A, B] = finite_elements(p, t, b)
    N = size(p, 1);
    T = size(t, 1);
    K = sparse(N, N); 
    F = zeros(N, 1);
    
    for e = 1 : T  % integration over one triangular element at a time
        nodes = t(e, :); % row of t = node numbers of the 3 corners of triangle e
        Pe = [ones(3, 1), p(nodes, :)]; % 3 by 3 matrix with rows=[1 xcorner ycorner]
        Area = abs(det(Pe)) / 2; % area of triangle e = half of parallelogram area
        C = inv(Pe); % columns of C are coeffs in a+bx+cy to give phi=1,0,0 at nodes
        % now compute 3 by 3 Ke and 3 by 1 Fe for element e
        grad = C(2 : 3, :);
        Ke = Area * grad' * grad; % element matrix from slopes b,c in grad
        Fe = - Area / 3; %* 4; % integral of phi over triangle is volume of pyramid: f(x,y)=4
        % multiply Fe by f at centroid for load f(x,y): one-point quadrature!
        % centroid would be mean(p(nodes,:)) = average of 3 node coordinates
        K(nodes, nodes) = K(nodes, nodes) + Ke; % add Ke to 9 entries of global K
        F(nodes) = F(nodes) + Fe; % add Fe to 3 components of load vector F
    end 
 
    % [Kb,Fb] = dirichlet(K,F,b) % assembled K was singular! K*ones(N,1)=0
    % Implement Dirichlet boundary conditions U(b)=0 at nodes in list b
    K(b, :) = 0;
    K(:, b) = 0; 
    F(b) = 0; % put zeros in boundary rows/columns of K and F
    K(b, b) = speye(length(b), length(b)); % put into boundary submatrix of K
    A = K; B = F; % Stiffness matrix Kb (sparse format) and load vector Fb
end

function [R] = originals(p)
    n = length(p);
    R = zeros(n, 1);
    
    for i = 1 : n
        R(i) = (p(i, 1)^2 + p(i, 2)^2 - 1) / 4;
    end
end

function [x] = conjgrad(A,b,x)
    r=b-A*x;
    p=r;
    rsold=r'*r;

    for i=1 : length(b)
        Ap = A * p;
        alpha = rsold / (p' * Ap);
        x = x + alpha * p;
        r = r - alpha * Ap; 
        rsnew = r' * r;
        if sqrt(rsnew) < 1e-10
              break;
        end
        p = r + (rsnew / rsold) * p;
        rsold = rsnew;
    end
end