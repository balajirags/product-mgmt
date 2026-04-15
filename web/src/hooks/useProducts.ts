import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { listProducts, getProduct, createProduct, updateProduct, deleteProduct, batchProducts } from '@/lib/api';
import type { ListProductsParams } from '@/lib/api';
import type { CreateProductRequest, UpdateProductRequest, BatchProductRequest } from '@/types/product';

export function useProducts(params: ListProductsParams = {}) {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => listProducts(params),
  });
}

export function useProduct(id: string) {
  return useQuery({
    queryKey: ['product', id],
    queryFn: () => getProduct(id),
    enabled: !!id,
  });
}

export function useCreateProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateProductRequest) => createProduct(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['products'] }),
  });
}

export function useUpdateProduct(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: UpdateProductRequest) => updateProduct(id, body),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['products'] });
      qc.invalidateQueries({ queryKey: ['product', id] });
    },
  });
}

export function useDeleteProduct() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteProduct(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['products'] }),
  });
}

export function useBatchProducts() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (body: BatchProductRequest) => batchProducts(body),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['products'] }),
  });
}
