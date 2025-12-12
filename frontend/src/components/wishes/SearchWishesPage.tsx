import {
  Button,
  Card,
  CardBody,
  CardHeader,
  Flex,
  Heading,
  Input,
  InputGroup,
  InputLeftElement,
  Text,
  Tabs,
  TabList,
  TabPanels,
  Tab,
  TabPanel,
} from "@chakra-ui/react";
import { SearchIcon } from "@chakra-ui/icons";
import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import type { WishPageResponse, WishResponse } from "../../api";
import { wishesApi } from "../../lib/api-client";
import { WishTable } from "./WishTable";
import { UserSearchPage } from "../users/UserSearchPage";

const PAGE_SIZE = 10;

export const SearchWishesPage = () => {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [page, setPage] = useState(1);
  const [tabIndex, setTabIndex] = useState(0);

  const wishesQuery = useQuery<WishPageResponse>({
    queryKey: ["search-wishes", searchQuery, page],
    queryFn: async () => {
      if (!searchQuery.trim()) {
        return { items: [], page: 0, size: 0, total_elements: 0, total_pages: 0 };
      }
      const response = await wishesApi.searchWishes(searchQuery, page - 1, PAGE_SIZE);
      return response.data;
    },
    enabled: searchQuery.trim().length > 0,
    placeholderData: keepPreviousData,
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    setSearchQuery(query);
    setPage(1);
  };

  const totalPages = Math.max(1, wishesQuery.data?.total_pages ?? 1);
  const safePage = Math.min(Math.max(page, 1), totalPages);
  const items = wishesQuery.data?.items ?? [];

  const handleSelect = (wish: WishResponse) => {
    navigate(`/wishes/${wish.wish_id}`);
  };

  return (
    <Card borderRadius="2xl" shadow="xl">
      <CardHeader>
        <Heading size="md">Поиск</Heading>
      </CardHeader>
      <CardBody>
        <Tabs index={tabIndex} onChange={setTabIndex}>
          <TabList>
            <Tab>По желаниям</Tab>
            <Tab>По пользователям</Tab>
          </TabList>
          <TabPanels>
            <TabPanel px={0}>
              <form onSubmit={handleSearch}>
                <InputGroup size="lg" mb={6}>
                  <InputLeftElement pointerEvents="none">
                    <SearchIcon color="gray.300" />
                  </InputLeftElement>
                  <Input
                    placeholder="Введите название или описание желания..."
                    value={query}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setQuery(e.target.value)}
                    bg="white"
                  />
                </InputGroup>
                <Button type="submit" colorScheme="purple" width="full" mb={6}>
                  Найти
                </Button>
              </form>

              {searchQuery && (
                <WishTable
                  items={items}
                  isLoading={wishesQuery.isLoading}
                  isError={Boolean(wishesQuery.isError)}
                  isEmpty={!wishesQuery.isLoading && items.length === 0}
                  page={safePage}
                  totalPages={totalPages}
                  onPrevPage={() => setPage((prev) => Math.max(1, prev - 1))}
                  onNextPage={() => setPage((prev) => Math.min(totalPages, prev + 1))}
                  onSelect={handleSelect}
                  showActions={false}
                />
              )}

              {!searchQuery && (
                <Flex direction="column" align="center" py={10} gap={2}>
                  <Text fontSize="lg" fontWeight="bold">
                    Введите поисковый запрос
                  </Text>
                  <Text color="gray.500">Начните поиск, чтобы найти желания других пользователей</Text>
                </Flex>
              )}
            </TabPanel>
            <TabPanel px={0}>
              <UserSearchPage />
            </TabPanel>
          </TabPanels>
        </Tabs>
      </CardBody>
    </Card>
  );
};

