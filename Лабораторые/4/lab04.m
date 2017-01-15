function [] = lab04()
    clc;
    clear all;
    
    solve();
    plot_det();
end

function [result] = generate_matrix(n)
    result = zeros(n, n);

    for i = 1 : n
        for j = 1 : n
            if (i == j)
                result(i, j) = 1;
            else if (i > j)
                    result(i, j) = 0;
                else
                    result(i, j) = -2;
                end
            end
        end
    end
end

function [result] = generate_vector(n)
    result = zeros(n, 1);

    for i = 1 : n    
        if (mod(i, 2) == 0)
            result(i) = -1 / 3;
        else 
            result(i) = 1;
        end
    end
end

function [] = plot_det()
    sizes = 1 : 32;
    conds = zeros(1, length(sizes));
    
    for n = 1 : length(sizes)
        size = sizes(n);
        m = generate_matrix(size);
        conds(n) = cond(m);
    end
    
    figure;
    
    plot(sizes, conds);
    
    xlabel('Matrix size') ;
    ylabel('Condition number'); 
    
    set(figure(4), 'Position', [150 150 1000 700]);
end

function [x] = backsub(u, b)
    [n, ~] = size(u);
    x = zeros(n, 1);
    x(n) = b(n) / u(n, n);
    
    for i = n - 1 : -1 : 1
        x(i) = (b(i) - u(i, i + 1 : n) * x(i + 1 : n)) / u(i, i);
    end 
end

function [result] = get_answer(n)
    result = zeros(n, 1);

    for i = 1 : n    
        if (mod(i, 2) == 0)
            result(i) = -1 / 3;
        else 
            result(i) = 1 / 3;
        end
    end
end

function [] = solve()
    close all

    sizes = 2 : 2 : 32;
    
    average_error_original = zeros(1, length(sizes));
    average_error_back_susb = zeros(1, length(sizes));
    average_error_qr = zeros(1, length(sizes));
    rel_average_error_original = zeros(1, length(sizes));
    rel_average_error_back_susb = zeros(1, length(sizes));
    rel_average_error_qr = zeros(1, length(sizes));
    abs_average_error_original = zeros(1, length(sizes));
    abs_average_error_back_susb = zeros(1, length(sizes));
    abs_average_error_qr = zeros(1, length(sizes));
    
    index = 1;
    
    for size = sizes            
        u = generate_matrix(size);
        b = generate_vector(size);
        
        original_answer = get_answer(size);
        
        solution = u \ b;
        residual_original = u * solution - b;
        average_error_original(index) = mean(abs(residual_original ./ b));
        
        rel_average_error_original(index) = mean(abs(original_answer - solution) ./ original_answer);
        abs_average_error_original(index) = mean(abs(original_answer - solution));
                
        solution = backsub(u, b);
        residual_back_substitution = u * solution - b;
        average_error_back_susb(index) = mean(abs(residual_back_substitution ./ b));
        
        rel_average_error_back_susb(index) = mean(abs(original_answer - solution) ./ original_answer);
        abs_average_error_back_susb(index) = mean(abs(original_answer - solution));
        
        [q, r] = houseTriangular(u);
        c = q' * b;
        solution = r \ c;
        residual_qr = u * solution - b;
        average_error_qr(index) = mean(abs(residual_qr ./ b));
        
        rel_average_error_qr(index) = mean(abs(original_answer - solution) ./ original_answer);
        abs_average_error_qr(index) = mean(abs(original_answer - solution));
        
        index = index + 1;
    end
 
    subplot(1, 3, 1);
    plot(sizes, average_error_back_susb);
    xlabel('Matrix size (backward substitution)'); 
    ylabel('Residual'); 
    subplot(1, 3, 2);
    plot(sizes, average_error_original);
    xlabel('Matrix size (matlab \ solution)');
    ylabel('Residual') 
    subplot(1, 3, 3);
    plot(sizes, average_error_qr);
    xlabel('Matrix size (qr householder)') ;
    ylabel('Residual'); 
    
    set(figure(1), 'Position', [0 0 1000 700]);
    
    figure;
    
    subplot(1, 3, 1);
    plot(sizes, rel_average_error_original);
    xlabel('Matrix size (backward substitution)') 
    ylabel('Relative error with analythical solution');
    subplot(1, 3, 2);
    plot(sizes, rel_average_error_back_susb);
    xlabel('Matrix size (matlab \ solution)') 
    ylabel('Relative error with analythical solution');
    subplot(1, 3, 3);
    plot(sizes, rel_average_error_qr);
    xlabel('Matrix size (qr householder)') 
    ylabel('Relative error with analythical solution');
    
    set(figure(2), 'Position', [50 50 1000 700]);
    
    figure;
    
    subplot(1, 3, 1);
    plot(sizes, abs_average_error_original);
    xlabel('Matrix size (backward substitution)') 
    ylabel('Absolute error with analythical solution');
    subplot(1, 3, 2);
    plot(sizes, abs_average_error_back_susb);
    xlabel('Matrix size (matlab \ solution)') 
    ylabel('Absolute error with analythical solution');
    subplot(1, 3, 3);
    plot(sizes, abs_average_error_qr);
    xlabel('Matrix size (qr householder)') 
    ylabel('Absolute error with analythical solution');
    
    set(figure(3), 'Position', [100 100 1000 700]);
end

function [q, r] = houseTriangular(u)

    [r, ~] = size(u); 
    q = eye(r);
 
    k = 1;
    for i = 1 : r - 1    
        v = zeros(1, r);
        v(k) = u(k, k) + sign(u(k, k)) * SpecSum(u(: , k), k);
        for j = k + 1 : r
            v(j) = u(j, k);
        end
        h = eye(r) - 2 * (conj(v' * v) / (v * v'));
        q = q * h;
        u = h * u;
        k = k + 1;
    end
 
    r = u;
end
   
function [result] = SpecSum(a, k)
    l = length(a);
    v = zeros(1, l);
    
    for i = k : l
        v(i) = a(i);
    end
    
    result = norm(v);
end