import numpy as np
import matplotlib.pyplot as plt

rng = np.random.default_rng()

distributions = {
    'lognormal': {
        'params': (3., 0.3, 50000),
        'generator': lambda p: rng.lognormal(p[0], p[1], p[2])
    },
    'weibull': {
        'params': (1., 2.5, 50000),
        'generator': lambda p: p[1] * rng.weibull(p[0], p[2])
    },
    'normal': {
        'params': (9., 2., 50000),
        'generator': lambda p: rng.normal(p[0], p[1], p[2])
    }
}

selected_distributions = ['lognormal', 'weibull', 'normal']
result_data = np.array([])
for dist_name in selected_distributions:
    dist = distributions[dist_name]
    data = dist['generator'](dist['params'])
    result_data = np.append(result_data, data)


with open("distribution_data/combined_data.txt", 'w') as f:
    f.write(f"{len(selected_distributions)}\n")

    for dist_name in selected_distributions:
        dist = distributions[dist_name]
        params = dist['params']
        f.write(f"{dist_name} {' '.join(map(str, params))}\n")

    np.savetxt(f, result_data, fmt='%.15f')


plt.hist(result_data, bins=100, density=True, alpha=0.6, color='g', label='Combined Data')
plt.xlabel('Value')
plt.ylabel('Density')
plt.legend()
plt.show()
