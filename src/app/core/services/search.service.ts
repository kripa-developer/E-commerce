import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';
import { ProductSummary, Page } from '../models';
import { environment } from '../../../environments/environment';

const BASE = environment.apiUrl;

export interface SearchSuggestion {
  type: 'product' | 'brand' | 'category';
  label: string;
  slug?: string;
  imageUrl?: string;
  price?: number;
  id?: number;
}

@Injectable({ providedIn: 'root' })
export class SearchService {
  constructor(private http: HttpClient) {}

  getSuggestions(keyword: string): Observable<SearchSuggestion[]> {
    if (!keyword || keyword.trim().length < 2) return of([]);

    const params = new HttpParams().set('keyword', keyword.trim()).set('page', '0').set('size', '5');
    return this.http.get<Page<ProductSummary>>(`${BASE}/products`, { params }).pipe(
      map(page => page.content.map(p => ({
        type: 'product' as const,
        label: p.name,
        slug: p.slug,
        imageUrl: p.primaryImageUrl,
        price: p.price,
        id: p.id
      }))),
      catchError(() => of([]))
    );
  }
}
